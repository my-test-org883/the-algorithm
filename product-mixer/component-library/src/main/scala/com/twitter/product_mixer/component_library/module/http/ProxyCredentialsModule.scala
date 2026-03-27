package com.twitter.product_mixer.component_library.module.http

import com.google.inject.Provides
import com.twitter.finagle.http.ProxyCredentials
import com.twitter.inject.TwitterModule
import com.twitter.inject.annotations.Flag
import com.twitter.util.security.{Credentials => CredentialsUtil}
import java.io.File
import javax.inject.Singleton

object ProxyCredentialsModule extends TwitterModule {
  final val HttpClientWithProxyCredentialsPath = "http_client.proxy.proxy_credentials_path"

  flag[String](HttpClientWithProxyCredentialsPath, "", "Path the load the proxy credentials")

  @Provides
  @Singleton
  def providesProxyCredentials(
    @Flag(HttpClientWithProxyCredentialsPath) proxyCredentialsPath: String,
  ): ProxyCredentials = {
    if (proxyCredentialsPath == null || proxyCredentialsPath.trim.isEmpty) {
      throw MissingProxyCredentialsException
    }

    val credentialsFile = new File(proxyCredentialsPath).getCanonicalFile

    // Disallow path traversal / unexpected file reads by requiring an existing, regular file.
    // This also avoids accepting directories or special files.
    if (!credentialsFile.exists() || !credentialsFile.isFile) {
      throw MissingProxyCredentialsException
    }

    ProxyCredentials(CredentialsUtil(credentialsFile))
      .getOrElse(throw MissingProxyCredentialsException)
  }

  object MissingProxyCredentialsException extends Exception("Proxy Credentials not found")
}
