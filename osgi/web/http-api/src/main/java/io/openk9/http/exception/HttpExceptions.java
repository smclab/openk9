package io.openk9.http.exception;

public class HttpExceptions {

	public static class NotFoundHttpException extends HttpException {
		public NotFoundHttpException(String reason) {
			super(404, reason);
		}
	}

	public static class InternalServerErrorHttpException
		extends HttpException {
		public InternalServerErrorHttpException(String reason) {
			super(500, reason);
		}
	}

}