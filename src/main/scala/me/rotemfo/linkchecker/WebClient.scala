package me.rotemfo.linkchecker

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

import org.asynchttpclient.DefaultAsyncHttpClient

import scala.concurrent.{Future, Promise}

/**
  * project: clustered-link-checker
  * package: me.rotemfo.linkchecker
  * file:    WebClient
  * created: 2019-03-04
  * author:  rotem
  */
trait WebClient {
  def get(url: String)(implicit executor: Executor): Future[String]
}

class AsyncWebClient extends WebClient {
  private var client = new DefaultAsyncHttpClient

  def get(url: String)(implicit executor: Executor): Future[String] = {
    try {
      if (client.isClosed) client = new DefaultAsyncHttpClient
      val f = client.prepareGet(url).execute()
      val p = Promise[String]()
      f.addListener(() => {
        val response = f.get()
        val statusCode = response.getStatusCode
        if (statusCode < 400)
          p.success(response.getResponseBody)
        else
          p.failure(BadStatus(statusCode))
      }, executor)
      p.future
    } catch {
      case e: IllegalArgumentException => Future.failed(e)
    }
  }

  def shutdown(): Unit = {
    client.close()
  }
}

object AsyncWebClient {
  private val refCount: AtomicInteger = new AtomicInteger(0)
  private var asyncWebClient: Option[AsyncWebClient] = None

  def getInstance(): AsyncWebClient = this.synchronized {
    if (asyncWebClient.isEmpty) asyncWebClient = Some(new AsyncWebClient())
    refCount.incrementAndGet()
    asyncWebClient.get
  }

  def shutdown(): Unit = {
    val ref = refCount.decrementAndGet()
    if (ref <= 0)
      asyncWebClient.get.shutdown()
  }
}