package com.garretwilson.net.http.webdav;

/**WebDAV HTTP methods as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV".
@author Garret Wilson
*/
public enum WebDAVMethod
{
	COPY,
	/*TODO decide if this is properly a WebDAV method CONNECT,*/
	DELETE,
	GET,
	HEAD,
	LOCK,
	MKCOL,
	MOVE,
	OPTIONS,
	PUT,
	POST,
	PROPFIND,
	PROPPATCH,
	TRACE,
	UNLOCK
}