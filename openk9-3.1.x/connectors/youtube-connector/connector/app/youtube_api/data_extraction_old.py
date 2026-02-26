import threading
import logging
import os
import base64
import re

import requests

from urllib.request import urlopen
from yt_dlp import YoutubeDL
from typing import List, Dict
from logging.config import dictConfig
from datetime import datetime
from .util.utility import format_raw_content, post_message

from .util.log_config import LogConfig

dictConfig(LogConfig().dict())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
	ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"

SIZE = 100
READ_BYTES_SIZE = 2048


class DataExtraction(threading.Thread):
	def __init__(self, youtube_channel_url, ydl_opts, subtitle_lang, timestamp, datasource_id, schedule_id, tenant_id):

		super(DataExtraction, self).__init__()
		self.youtube_channel_url = youtube_channel_url
		self.ydl_opts = ydl_opts
		self.subtitle_lang = subtitle_lang
		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.status_logger = logging.getLogger("youtube_logger")

		self.size = SIZE
		self.read_bytes_size = READ_BYTES_SIZE

	def manage_data(self, entries: List[Dict]):
		count = 0
		end_timestamp = datetime.utcnow().timestamp() * 1000
		for entry in entries:
			audio = None

			for frmt in entry['formats']:
				if frmt['format'] == entry['format']:
					f = bytearray()
					with urlopen(frmt['url']) as url:
						while True:
							b = url.read(self.read_bytes_size)
							self.status_logger.info(f'downloaded {len(b)} bytes. {len(f)}/{url.headers["content-length"]}')
							if not b:
								break
							f.extend(b)
					audio = base64.b64encode(f)

			categories = entry['categories'] or []
			comment_count = entry['comment_count'] or 0
			description = entry['description'] or ''
			duration = entry['duration'] or 0
			duration_string = entry['duration_string'] or ''
			title = entry['fulltitle'] or entry['title'] or ''
			like_count = entry['like_count'] or 0
			resolution = entry['resolution'] or ''
			tags = entry['tags'] or []
			upload_date = entry['upload_date'] or 0
			view_count = entry['view_count'] or 0
			was_live = entry['was_live'] or False
			webpage_url = entry['webpage_url'] or ''
			display_id = entry['display_id'] or ''

			subtitles = ''
			if entry['requested_subtitles'] and entry['requested_subtitles'][self.subtitle_lang]:
				response = requests.get(entry['requested_subtitles'][self.subtitle_lang]['url'], stream=True)
				subtitles = re.sub(r'\d{2}\W\d{2}\W\d{2}\W\d{3}\s\W{3}\s\d{2}\W\d{2}\W\d{2}\W\d{3}', '', response.text)
				self.status_logger.info(f"Downloaded {'manual' if len(entry['subtitles']) > 0 else 'automatic'} captions for {title} Youtube video")
			else:
				self.status_logger.info(f'Youtube video {title} does not have any {self.subtitle_lang} captions')

			raw_content_elements = [str(title or ''), str(description or '')]
			raw_content = format_raw_content(''.join(raw_content_elements))

			content_id = display_id

			datasource_payload = {
				'title': title,
				'description': description,
				'categories': categories,
				'commentCount': comment_count,
				'duration': duration,
				'durationString': duration_string,
				'viewCount': view_count,
				'likeCount': like_count,
				'tags': tags,
				'resolution': resolution,
				'subtitles': subtitles,
				'uploadDate': upload_date,
				'wasLive': was_live,
				'webpageUrl': webpage_url
			}

			binary = {
				"id": content_id,
				"name": content_id,
				"contentType": "",
				"data": audio,
				"resourceId": None
			}

			payload = {
				"datasourceId": self.datasource_id,
				"scheduleId": self.schedule_id,
				"tenantId": self.tenant_id,
				"contentId": content_id,
				"parsingDate": int(end_timestamp),
				"rawContent": raw_content,
				"datasourcePayload": datasource_payload,
				"resources": {
					"binaries": [
						binary
					]
				}
			}
			try:
				self.status_logger.info(datasource_payload)
				# post_message(ingestion_url, payload, 10)
				count = count + 1
			except requests.RequestException:

				self.status_logger.error("Problems during posting")

				continue
		return count

	def extract_recent(self):
		video_count = 0
		with YoutubeDL(self.ydl_opts) as ydl:
			info = ydl.extract_info(self.youtube_channel_url, download=False, process=False)
			self.status_logger.info("element type: ", type(info))
			#video_count = self.manage_data(info['entries'])

		self.status_logger.info('Extracted: ' + str(video_count) + ' videos')
