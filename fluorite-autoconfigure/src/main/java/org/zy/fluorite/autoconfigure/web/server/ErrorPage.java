package org.zy.fluorite.autoconfigure.web.server;

import org.zy.fluorite.web.http.HttpStatus;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description 错误页的简单服务器独立抽象
 */
public class ErrorPage {
	private final HttpStatus status;

	private final Class<? extends Throwable> exception;

	private final String path;

	public ErrorPage(String path) {
		this.status = null;
		this.exception = null;
		this.path = path;
	}

	public ErrorPage(HttpStatus status, String path) {
		this.status = status;
		this.exception = null;
		this.path = path;
	}

	public ErrorPage(Class<? extends Throwable> exception, String path) {
		this.status = null;
		this.exception = exception;
		this.path = path;
	}

	/**
	 * 要呈现的路径(通常作为forward实现)，以“/”开头。可以使用自定义控制器或servlet路径，如果服务器支持，也可以使用模板路径(例如。“/error.html”)。
	 * @return 此错误将被呈现的路径
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * 返回异常类型(或根据状态匹配的页面为null)
	 * @return 异常类型或null
	 */
	public Class<? extends Throwable> getException() {
		return this.exception;
	}

	/**
	 * 这个错误页面匹配的HTTP状态值(或者为null，对于通过异常匹配的页面)。
	 * @return 状态或null
	 */
	public HttpStatus getStatus() {
		return this.status;
	}

	/**
	 * 此错误页面匹配的HTTP状态值。
	 * @return 状态值(对于匹配任何状态的页面，为0)
	 */
	public int getStatusCode() {
		return (this.status != null) ? this.status.value() : 0;
	}

	/**
	 * 异常类型名称。
	 * @return 异常类型名称(如果没有，则为空)
	 */
	public String getExceptionName() {
		return (this.exception != null) ? this.exception.getName() : null;
	}

	/**
	 * 如果该错误页是全局错误页则返回(匹配所有不匹配的状态和异常类型)。
	 * @return 如果这是一个全局错误页面
	 */
	public boolean isGlobal() {
		return (this.status == null && this.exception == null);
	}
}
