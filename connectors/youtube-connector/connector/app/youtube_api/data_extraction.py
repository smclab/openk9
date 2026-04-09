import datetime
import json
import logging
import os
import random
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed
from logging.config import dictConfig
from typing import Dict

import requests
from yt_dlp import YoutubeDL

from .util.log_config import LogConfig
from .util.utility import format_raw_content, post_message, hash_str_to_int, get_as_base64, FutureResult, FileData
from .util.yt_dlp_logger import YtDlpLogger

dictConfig(LogConfig().dict())

ingestion_url = os.environ.get("INGESTION_URL")
if ingestion_url is None:
	ingestion_url = "http://openk9-ingestion:8080/v1/ingestion/"

pot_provider_url = os.environ.get("POT_PROVIDER_URL")

OUTPUT_TEMPLATE = "/tmp/%(id)s.%(ext)s"
MAX_WORKERS = 5


class DataExtraction(threading.Thread):
	def __init__(self, youtube_channel_url, subtitle_lang, do_extract_audio, audio_format, do_extract_comments, max_comments, socket_timeout, sleep_interval_subtitles, sleep_interval_requests, sleep_interval, do_use_random_wait_time, max_sleep_interval, retries_count, max_read_bytes_size, verbose, timestamp, datasource_id, schedule_id, tenant_id):

		super(DataExtraction, self).__init__()
		self.youtube_channel_url = youtube_channel_url
		self.subtitle_lang = subtitle_lang
		self.do_extract_audio = do_extract_audio
		self.audio_format = audio_format
		self.timestamp = timestamp
		self.datasource_id = datasource_id
		self.schedule_id = schedule_id
		self.tenant_id = tenant_id

		self.socket_timeout = socket_timeout
		self.sleep_interval_subtitles = sleep_interval_subtitles
		self.sleep_interval_requests = sleep_interval_requests
		self.sleep_interval = sleep_interval
		self.do_use_random_wait_time = do_use_random_wait_time
		self.max_sleep_interval = max_sleep_interval
		self.retries_count = retries_count
		self.max_read_bytes_size = max_read_bytes_size

		self.verbose = verbose

		self.do_extract_comments = do_extract_comments
		self.max_comments = max_comments

		self.status_logger = logging.getLogger("youtube_logger")

		common_youtube_args = {
			'max_comments': self.max_comments,
			'player_client': ['default'],
			# 'player_client': ['default'],
		}

		data_range_start = datetime.date.fromtimestamp(self.timestamp/1000).strftime('%Y%m%d')

		pot_args = {}
		if pot_provider_url:
			# TODO: Check why bgutil never gets called
			pot_args = {'youtubepot-bgutilhttp': {'base_url': [pot_provider_url]}}
			common_youtube_args.update({
				'pot_provider': ['bgutil'],
				'fetch_pot': ['always'],  # Forces PO Token
			})

		self.ydl_options_flat = {
			'ignoreerrors': True,
			'extract_flat': 'in_playlist',  # Gets only video urls
			'dateafter': data_range_start,
			'extractor_args': {'youtube': common_youtube_args},
			'socket_timeout': self.socket_timeout,
			'js_runtimes': {'node': {}},
		}

		post_processors = []
		if self.do_extract_audio:
			post_processors.append({
				'key': 'FFmpegExtractAudio',
				'preferredcodec': self.audio_format,  # Final extension
			})

		# TODO: Add as request parameter (bool)
		do_write_subtitles = self.subtitle_lang is not None and len(self.subtitle_lang) > 0
		self.ydl_options_download = {
			'skip_download': not self.do_extract_audio,  # Skips download if not extract audio
			'format': 'bestaudio/best',  # Download audio only
			'outtmpl': OUTPUT_TEMPLATE,
			'ignoreerrors': True,

			# --- Subtitles Download ---
			'writesubtitles': do_write_subtitles,  # User generated subtitles
			'writeautomaticsub': do_write_subtitles,  # Automatic subtitles
			'subtitleslangs': self.subtitle_lang,  # list on languages
			# TODO: Add as request parameter (Enum)
			'subtitlesformat': 'srv1',
			# --------------------------

			'extract_flat': False,
			'playlist_items': '1',
			'noplaylist': True,
			'getcomments': self.do_extract_comments,
			'writecomments': self.do_extract_comments,
			'extractor_args': {
				'youtube': common_youtube_args,
				**pot_args  # Include pot provider if added
			},
			'postprocessors': post_processors,
			'socket_timeout': self.socket_timeout,
			'js_runtimes': {'node': {}},

			# --- RETRY + TIMEOUT HANDLERS ---
			'retries': self.retries_count,  # Generic error retry
			'fragment_retries': self.retries_count,  # Retries for audio fragments
			'retry_sleep_functions': {  # Wait time strategy
				'http': lambda n: random.uniform(1, 5)  # Waits between 1 and 5 seconds randomly
			},
			# --------------------------------

			# --- ANTI-BOT ---
			'sleep_interval': self.sleep_interval,  # Wait time between downloads
			'max_sleep_interval': self.max_sleep_interval if self.do_use_random_wait_time else self.sleep_interval,  # Random wait time
			'sleep_interval_requests': self.sleep_interval_requests,  # Wait time between HTTP requests
			'sleep_interval_subtitles': self.sleep_interval_subtitles,  # Sleep for subtitles: suggested 60
			# ----------------

			'ratelimit': self.max_read_bytes_size,  # Download speed limit, in bytes/sec.
			'verbose': self.verbose,  # Verbose logs
		}

		if self.do_extract_audio:
			self.ydl_options_download.update({
				'skip_download': True,
			})

		if not self.do_extract_audio:
			# Does not download media
			self.ydl_options_download.update({
				'skip_download': True,
				'postprocessors': [{
					'key': 'FFmpegExtractAudio',
					'preferredcodec': self.audio_format,  # Final extension
				}]
			})
			# Removes postprocessor for audio, better performance
			self.ydl_options_download['postprocessors'] = [
				p for p in self.ydl_options_download['postprocessors'] if p.get('key') != 'FFmpegExtractAudio'
			]

		self.max_workers = MAX_WORKERS

	def manage_data(self, entry: Dict, audio_file_data: FileData | None, subtitle_files_data: dict[str, FileData]) -> bool:
		end_timestamp = datetime.datetime.now(datetime.timezone.utc).timestamp() * 1000

		try:
			self.status_logger.info(f"keys: {entry.keys()}")
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
			like_count = entry['like_count']
			channel_follower_count = entry['channel_follower_count']
			uploader_id = entry['uploader_id']
			timestamp = entry['timestamp']
			availability = entry['availability']

			raw_content_elements = [str(title or ''), str(description or '')]
			raw_content = format_raw_content(''.join(raw_content_elements))

			content_id = hash_str_to_int(str(video_id))

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
				'ageLimit': age_limit,
				'liveStatus': live_status,
				'webpageUrl': webpage_url,
				'heatmap': heatmap,
				'channelFollowerCount': channel_follower_count,
				'uploaderId': uploader_id,
				'uploadTime': int(timestamp) * 1000,
				'availability': availability
			}

			binaries = []

			if self.do_extract_audio and audio_file_data:
				self.status_logger.info(f"Audio binary extraction")
				binary = {
					"id": hash_str_to_int(audio_file_data.name),
					"name": audio_file_data.name,
					"contentType": "",
					"data": audio_file_data.data,
				}
				binaries.append(binary)
				self.status_logger.info(f"Audio binary data extracted")

			if self.subtitle_lang and subtitle_files_data:
				for lang, subtitle_file_data in subtitle_files_data.items():
					self.status_logger.info(f"{lang}: Subtitles binary extraction")
					if not subtitle_file_data:
						self.status_logger.warning(f"Skipped {lang}: Missing subtitles data")
						continue
					binary = {
						"id": hash_str_to_int(subtitle_file_data.name),
						"name": subtitle_file_data.name,
						"contentType": "",
						"data": subtitle_file_data.data,
					}
					binaries.append(binary)
					self.status_logger.info(f"{lang}: Subtitles binary extracted")

			if self.do_extract_comments:
				self.status_logger.info(f"Comments extraction")
				data = json.dumps(entry['comments']).encode()
				file_name = f"{video_id}.comments.json"
				binary = {
					"id": hash_str_to_int(file_name),
					"name": file_name,
					"contentType": "",
					"data": get_as_base64(data),
				}
				binaries.append(binary)
				self.status_logger.info(f"Comments binary extracted")

			payload = {
				"datasourceId": self.datasource_id,
				"scheduleId": self.schedule_id,
				"tenantId": self.tenant_id,
				"contentId": content_id,
				"parsingDate": int(end_timestamp),
				"rawContent": raw_content,
				"datasourcePayload": {"video": datasource_payload},
				"resources": {
					"binaries": binaries
				}
			}

			try:
				self.status_logger.info(datasource_payload)
				post_message(ingestion_url, payload, 10)
				return True
			except requests.RequestException:
				self.status_logger.error("Problems during posting")
				return False
		except Exception as e:
			payload = {
				"datasourceId": self.datasource_id,
				"scheduleId": self.schedule_id,
				"tenantId": self.tenant_id,
				"contentId": -1,
				"parsingDate": int(end_timestamp),
				"rawContent": e,
				"datasourcePayload": {

				},
				"resources": {
					"binaries": []
				},
				"type": "HALT"
			}
			self.status_logger.error(e)
			post_message(ingestion_url, payload, 10)
			return False

	# Thread worker handles IO Tasks
	def __thread_worker(self, url):
		self.ydl_options_download['logger'] = YtDlpLogger(url, verbose=True)
		audio_file_path = ""
		subtitle_files = []

		with YoutubeDL(self.ydl_options_download) as ydl:
			try:
				# Downloads audio file in temp folder
				video_info = ydl.extract_info(url, download=True, process=True)

				if not video_info:
					self.status_logger.error(f"Could not extract video: {url}")
					raise Exception(f"Could not extract video: {url}")

				# Extracts audio file data
				audio_file_data: FileData | None = None
				if self.do_extract_audio:
					audio_file_path = video_info.get('filepath') or ""
					if not audio_file_path or not os.path.exists(audio_file_path):
						audio_file_path = ydl.prepare_filename(video_info, warn=True)
						if not os.path.exists(audio_file_path):
							base = os.path.splitext(audio_file_path)[0]
							audio_file_path = f"{base}.{self.audio_format}"

					if os.path.exists(audio_file_path):
						with open(audio_file_path, 'rb') as f:
							audio_file_data = FileData(
								name=os.path.basename(audio_file_path),
								data=get_as_base64(f.read())
							)
					else:
						# Exits when no file
						self.status_logger.error(f"Could not extract video audio: {url}")
						raise Exception(f"Could not extract video audio: {url}")

				subtitle_files_data: dict[str, FileData] = {}
				if self.subtitle_lang:
					downloaded_subtitles = video_info.get('requested_subtitles') or {}
					for lang, sub_info in downloaded_subtitles.items():
						sub_path = sub_info.get('filepath') or ""
						if not sub_path or not os.path.exists(sub_path):
							self.status_logger.warning(f"Skipped lang: {lang}")
							continue

						subtitle_files.append(sub_path)
						with open(sub_path, 'rb') as f:
							subtitle_files_data[lang] = FileData(
								name=os.path.basename(sub_path),
								data=get_as_base64(f.read())
							)

				return FutureResult(
					url=url,
					video_info=video_info,
					audio_file_data=audio_file_data,
					subtitle_files_data=subtitle_files_data
				)
			except Exception as e:
				self.status_logger.warning(f"Extraction failed for {url}: {e}")
				raise
			finally:
				# Delete Files on extraction completed
				self.status_logger.info(f"Removing temporary files if created for: {url}")
				if audio_file_path and os.path.exists(audio_file_path):
					os.remove(audio_file_path)
					self.status_logger.info(f"Removed temporary audio file {audio_file_path}")

				for sub_path in subtitle_files:
					if os.path.exists(sub_path):
						os.remove(sub_path)
						self.status_logger.info(f"Removed temporary subtitle file {sub_path}")

	def __get_entries(self, info):
		count = 0
		futures = []
		with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
			if 'entries' in info:
				for entry in info['entries']:
					# Used on url endpoint /@channel
					if entry.get('extractor_key') == 'YoutubeTab':
						self.status_logger.info("Used on url endpoint /@channel YoutubeTab")
						for video in entry['entries']:
							# Each item is a video
							futures.append(executor.submit(self.__thread_worker, video['url']))
					else:
						# Used on url endpoint /@channel/video, /shorts, /streams
						# Each entry is a video
						self.status_logger.info("Used on url endpoint /@channel + /video | /shorts | /streams")
						futures.append(executor.submit(self.__thread_worker, entry['url']))
			else:
				# Extract single video
				self.status_logger.info("Used on single video")
				futures.append(executor.submit(self.__thread_worker, info['original_url']))

			# Wait for all I/O-bound tasks to complete
			for future in as_completed(futures):
				try:
					result: FutureResult = future.result()
					# url, info, audio_data, subtitle_data = future.result()
					extraction = self.manage_data(result.video_info, result.audio_file_data, result.subtitle_files_data)
					count += extraction

					self.status_logger.info(f"Processed {result.url}: Result {'Success' if extraction else 'Failure'}")

					# Free memory
					del result
				except Exception as e:
					self.status_logger.error(f"Exception processing future: {e}")
		return count

	def extract_recent(self):
		# Extracts urls
		with YoutubeDL(self.ydl_options_flat) as ydl:
			info = ydl.extract_info(self.youtube_channel_url, download=False, process=False)
			count = self.__get_entries(info)

			self.status_logger.info('Extracted: ' + str(count) + ' videos')
		self.post_last()

	def post_last(self):
		end_timestamp = datetime.datetime.now(datetime.timezone.utc).timestamp() * 1000

		payload = {
			"datasourceId": self.datasource_id,
			"parsingDate": int(end_timestamp),
			"contentId": None,
			"rawContent": None,
			"datasourcePayload": {},
			"resources": {
				"binaries": []
			},
			"acl": {
				"type": ["deleted"]
			},
			"scheduleId": self.schedule_id,
			"tenantId": self.tenant_id,
			"last": True
		}

		post_message(ingestion_url, payload)

