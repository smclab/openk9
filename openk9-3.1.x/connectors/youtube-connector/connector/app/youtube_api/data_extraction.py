import threading
import logging
import os
import base64
import requests

from concurrent.futures import ThreadPoolExecutor, as_completed
from urllib.request import urlopen
from yt_dlp import YoutubeDL
from yt_dlp.utils import DownloadError, DateRange
from typing import List, Dict
from logging.config import dictConfig
from datetime import datetime
from .util.utility import format_raw_content, post_message

from .util.log_config import LogConfig

dictConfig(LogConfig().dict())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
	ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"

READ_BYTES_SIZE = 2048
MAX_WORKERS = 5


class DataExtraction(threading.Thread):
	def __init__(self, youtube_channel_url, subtitle_lang, audio_format, timestamp, datasource_id, schedule_id, tenant_id):

		super(DataExtraction, self).__init__()
		self.youtube_channel_url = youtube_channel_url
		self.subtitle_lang = subtitle_lang
		self.audio_format = audio_format
		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.status_logger = logging.getLogger("youtube_logger")

		data_range_start = datetime.utcfromtimestamp(self.timestamp).strftime('%Y%m%d')
		self.ydl_options = {'writeautomaticsub': True, 'subtitlesformat': 'srv1', 'format': 'bestaudio', 'daterange': DateRange(start=data_range_start)}

		self.read_bytes_size = READ_BYTES_SIZE
		self.max_workers = MAX_WORKERS

		self.video_count = 0

	def manage_data(self, entry: Dict):
		end_timestamp = datetime.utcnow().timestamp() * 1000
		audio = None

		for frmt in entry['formats']:
			if frmt['ext'] == self.audio_format:
				f = bytearray()
				with urlopen(frmt['url']) as url:
					while True:
						b = url.read(self.read_bytes_size)
						self.status_logger.info(f"downloaded {len(b)} bytes. {len(f)}/{url.headers['content-length']}")
						if not b:
							break
						f.extend(b)
				audio = base64.b64encode(f)
				break

		video_id = entry['id']
		title = entry['title']
		thumbnail = entry['thumbnail']
		description = entry['description']
		channel_id = entry['channel_id']
		duration = entry['duration']
		view_count = entry['view_count']
		age_limit = entry['age_limit']
		webpage_url = entry['webpage_url']
		categories = entry['categories']
		tags = entry['tags']
		live_status = entry['live_status']
		comment_count = entry['comment_count']
		heatmap = entry['heatmap']
		like_count = ['like_count']
		channel_follower_count = entry['channel_follower_count']
		uploader_id = entry['uploader_id']
		timestamp = entry['timestamp']
		availability = entry['availability']

		subtitles = {}
		for lang in self.subtitle_lang:
			if entry['automatic_captions'] and entry['automatic_captions'][lang]:
				for el in entry['automatic_captions'][lang]:
					if el['ext'] == self.ydl_options['subtitlesformat']:
						subtitles[lang] = requests.get(el['url']).text
						break

		raw_content_elements = [str(title or ''), str(description or '')]
		raw_content = format_raw_content(''.join(raw_content_elements))

		content_id = video_id

		datasource_payload = {
			'title': title,
			'description': description,
			'categories': categories,
			'commentCount': comment_count,
			'duration': duration,
			'thumbnail': thumbnail,
			'viewCount': view_count,
			'likeCount': like_count,
			'tags': tags,
			'channelId': channel_id,
			'subtitles': subtitles,
			'ageLimit': age_limit,
			'liveStatus': live_status,
			'webpageUrl': webpage_url,
			'heatmap': heatmap,
			'channelFollowerCount': channel_follower_count,
			'uploaderId': uploader_id,
			'uploadTime': timestamp,
			'availability': availability
		}

		binary = {
			"id": content_id,
			"name": title,
			"contentType": "",
			"data": audio,
		}

		payload = {
			"datasourceId": self.datasource_id,
			"scheduleId": self.schedule_id,
			"tenantId": self.tenant_id,
			"contentId": content_id,
			"parsingDate": int(end_timestamp),
			"rawContent": raw_content,
			"datasourcePayload": {"video": datasource_payload},
			"resources": {
				"binaries": [
					binary
				]
			}
		}
		try:
			self.status_logger.info(datasource_payload)
			post_message(ingestion_url, payload, 10)
			self.video_count = self.video_count + 1
		except requests.RequestException:
			self.status_logger.error("Problems during posting")

	def __get_entries(self, ydl, info, executor):
		if 'entries' in info:
			futures = []
			for entry in info['entries']:
				if 'extractor_key' in entry and entry['extractor_key'] == 'YoutubeTab':
					for entry2 in entry['entries']:
						info2 = ydl.extract_info(entry2['url'], download=False, process=False)
						future = executor.submit(self.__get_entries, ydl, info2, executor)
						futures.append(future)
				try:
					info2 = ydl.extract_info(entry['url'], download=False, process=False)
					future = executor.submit(self.__get_entries, ydl, info2, executor)
					futures.append(future)
					# self.__get_entries(ydl, info2)
				except DownloadError as e:
					self.status_logger.error(f'Problems during extraction: {e}')

			# Wait for all tasks in this level to finish
			for future in as_completed(futures):
				future.result()  # Ensure exceptions are raised
		else:
			# Process the individual video entry in a new thread
			executor.submit(self.manage_data, info)

	def extract_recent(self):
		with YoutubeDL(self.ydl_options) as ydl, ThreadPoolExecutor(max_workers=self.max_workers) as executor:
			info = ydl.extract_info(self.youtube_channel_url, download=False, process=False)
			self.__get_entries(ydl, info, executor)

		self.status_logger.info('Extracted: ' + str(self.video_count) + ' videos')
