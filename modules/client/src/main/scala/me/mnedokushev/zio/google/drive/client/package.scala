package me.mnedokushev.zio.google.drive

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.{ Drive, DriveScopes }
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import zio._

import java.io.IOException
import java.util

package object client {

  def driverLive(appName: String): ZLayer[Any, IOException, Drive] =
    ZLayer.fromZIO(
      ZIO.attemptBlockingIO {
        val credentials        = GoogleCredentials.getApplicationDefault().createScoped(util.Arrays.asList(DriveScopes.DRIVE))
        val requestInitializer = new HttpCredentialsAdapter(credentials)

        new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance, requestInitializer)
          .setApplicationName(appName)
          .build()
      }
    )

}
