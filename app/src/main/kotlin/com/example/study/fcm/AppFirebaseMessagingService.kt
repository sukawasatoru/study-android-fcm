/*
 * Copyright 2024 sukawasatoru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.study.fcm

import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.Constants.MessageNotificationKeys
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.PrintWriter
import java.io.StringWriter

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onCreate() {
        log("onCreate")

        super.onCreate()
    }

    override fun onDestroy() {
        log("onDestroy")

        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val messageString = StringWriter().let { stringWriter ->
            PrintWriter(stringWriter).use {
                message.dump("", it)
            }
            stringWriter.toString()
        }

        log("onMessageReceived: $messageString")
    }

    override fun onNewToken(token: String) {
        log("onNewToken: $token")
    }

    private fun log(msg: String) = Log.i("AppFirebaseMessagingService", msg)
}

private val refRemoteMessageBundleField by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val dataField = RemoteMessage::class.java.getDeclaredField("bundle")
    dataField.isAccessible = true
    dataField
}

fun RemoteMessage.dump(prefix: String, writer: PrintWriter) {
    val innerPrefix = "$prefix  "
    val innerPrefix2 = "$innerPrefix  "

    writer.append(prefix)
    writer.appendLine("RemoteMessage{")

    writer.print(innerPrefix)
    writer.print("getSenderId: ")
    writer.println(senderId)

    writer.print(innerPrefix)
    writer.print("getFrom: ")
    writer.println(from)

    writer.print(innerPrefix)
    writer.print("to: ")
    writer.println(to)

    writer.print(innerPrefix)
    writer.println("getData:")
    for ((key, value) in data) {
        writer.print(innerPrefix2)
        writer.print(key)
        writer.print(": ")
        writer.println(value)
    }

    writer.print(innerPrefix)
    writer.print("getCollapseKey: ")
    writer.println(collapseKey)

    writer.print(innerPrefix)
    writer.print("getMessageId: ")
    writer.println(messageId)

    writer.print(innerPrefix)
    writer.print("getMessageType: ")
    writer.println(messageType)

    writer.print(innerPrefix)
    writer.print("getSentTime: ")
    writer.println(sentTime)

    writer.print(innerPrefix)
    writer.print("getTtl: ")
    writer.println(ttl)

    writer.print(innerPrefix)
    writer.print("getOriginalPriority: ")
    writer.println(originalPriority)

    writer.print(innerPrefix)
    writer.print("getPriority: ")
    writer.print(priority)
    writer.print('(')
    when (priority) {
        RemoteMessage.PRIORITY_UNKNOWN -> writer.print("PRIORITY_UNKNOWN")
        RemoteMessage.PRIORITY_HIGH -> writer.print("PRIORITY_HIGH")
        RemoteMessage.PRIORITY_NORMAL -> writer.print("PRIORITY_NORMAL")
        else -> Unit
    }
    writer.println(')')

    writer.print(innerPrefix)
    writer.println("getNotification:")
    notification?.let { notification ->
        writer.print(innerPrefix2)
        writer.print("getTitle: ")
        writer.println(notification.title)

        writer.print(innerPrefix2)
        writer.print("getTitleLocalizationKey: ")
        writer.println(notification.titleLocalizationKey)

        writer.print(innerPrefix2)
        writer.print("getTitleLocalizationArgs: ")
        writer.println(notification.titleLocalizationArgs?.joinToString())

        writer.print(innerPrefix2)
        writer.print("getBody: ")
        writer.println(notification.body)

        writer.print(innerPrefix2)
        writer.print("getBodyLocalizationKey: ")
        writer.println(notification.bodyLocalizationKey)

        writer.print(innerPrefix2)
        writer.print("getBodyLocalizationArgs: ")
        writer.println(notification.bodyLocalizationArgs?.joinToString())

        writer.print(innerPrefix2)
        writer.print("getIcon: ")
        writer.println(notification.icon)

        writer.print(innerPrefix2)
        writer.print("getImageUrl: ")
        writer.println(notification.imageUrl)

        writer.print(innerPrefix2)
        writer.print("getSound: ")
        writer.println(notification.sound)

        writer.print(innerPrefix2)
        writer.print("getTag: ")
        writer.println(notification.tag)

        writer.print(innerPrefix2)
        writer.print("getColor: ")
        writer.println(notification.color)

        writer.print(innerPrefix2)
        writer.print("getClickAction: ")
        writer.println(notification.clickAction)

        writer.print(innerPrefix2)
        writer.print("getChannelId: ")
        writer.println(notification.channelId)

        writer.print(innerPrefix2)
        writer.print("getLink: ")
        writer.println(notification.link)

        writer.print(innerPrefix2)
        writer.print("getTicker: ")
        writer.println(notification.ticker)

        writer.print(innerPrefix2)
        writer.print("getSticky: ")
        writer.println(notification.sticky)

        writer.print(innerPrefix2)
        writer.print("getLocalOnly: ")
        writer.println(notification.localOnly)

        writer.print(innerPrefix2)
        writer.print("getDefaultSound: ")
        writer.println(notification.defaultSound)

        writer.print(innerPrefix2)
        writer.print("getDefaultVibrateSettings: ")
        writer.println(notification.defaultVibrateSettings)

        writer.print(innerPrefix2)
        writer.print("getDefaultLightSettings: ")
        writer.println(notification.defaultLightSettings)

        writer.print(innerPrefix2)
        writer.print("getNotificationPriority: ")
        writer.println(notification.notificationPriority)

        writer.print(innerPrefix2)
        writer.print("getVisibility: ")
        writer.println(notification.visibility)

        writer.print(innerPrefix2)
        writer.print("getNotificationCount: ")
        writer.println(notification.notificationCount)

        writer.print(innerPrefix2)
        writer.print("getEventTime: ")
        writer.println(notification.eventTime)

        writer.print(innerPrefix2)
        writer.print("getLightSettings: ")
        writer.println(notification.lightSettings?.joinToString())

        writer.print(innerPrefix2)
        writer.print("getVibrateTimings: ")
        writer.println(notification.vibrateTimings?.joinToString())
    }

    writer.print(innerPrefix)
    writer.println("bundle:")
    (refRemoteMessageBundleField.get(this) as Bundle).let { bundle ->
        writer.print(innerPrefix2)
        writer.print(MessageNotificationKeys.ENABLE_NOTIFICATION)
        writer.print("(ENABLE_NOTIFICATION): ")
        writer.println(bundle.getString(MessageNotificationKeys.ENABLE_NOTIFICATION))

        writer.print(innerPrefix2)
        writer.print("notification.n.e(ENABLE_NOTIFICATION (OLD)): ")
        writer.println(bundle.getString("notification.n.e"))
    }

    writer.print('}')
}
