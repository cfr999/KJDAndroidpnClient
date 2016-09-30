/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidpn.client;

import android.content.Intent;
import android.util.Log;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import java.text.SimpleDateFormat;
import java.util.Date;

/** 
 * This class notifies the receiver of incoming notifcation packets asynchronously.  
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationPacketListener implements PacketListener {

    private static final String LOGTAG = LogUtil
            .makeLogTag(NotificationPacketListener.class);

    private final XmppManager xmppManager;

    public NotificationPacketListener(XmppManager xmppManager) {
        this.xmppManager = xmppManager;
    }

    @Override
    public void processPacket(Packet packet) {
        Log.d(LOGTAG, "NotificationPacketListener.processPacket()...");
        Log.d(LOGTAG, "packet.toXML()=" + packet.toXML());

        if (packet instanceof NotificationIQ) {
            NotificationIQ notification = (NotificationIQ) packet;

            if (notification.getChildElementXML().contains(
                    "androidpn:iq:notification")) {
                String notificationId = notification.getId();
                String notificationApiKey = notification.getApiKey();
                String notificationTitle = notification.getTitle();
                String notificationMessage = notification.getMessage();
                //                String notificationTicker = notification.getTicker();
                String notificationUri = notification.getUri();
				String notificationimageUri = notification.getImageUrl();
				
				String notificationvideoUrl = notification.getVideoUrl();
				String notificationmusicUrl = notification.getMuisicUrl();
				
//				Log.d("NotificationPacketListener", "notificationId :" + notificationId);
//				Log.d("NotificationPacketListener", "notificationTitle :" + notificationTitle);
//				Log.d("NotificationPacketListener", "notificationMessage :" + notificationMessage);
//				Log.d("NotificationPacketListener", "notificationvideoUrl :" + notificationvideoUrl);
				
				NotificationHistory history = new NotificationHistory();
				history.setApiKey(notificationApiKey);
				history.setImageUrl(notificationimageUri);
				history.setMessage(notificationMessage);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String time = sdf.format(new Date());
				history.setTime(time);
				history.setTitle(notificationTitle);
				history.setUri(notificationUri);
				history.setMusicUrl(notificationmusicUrl);
				history.setVideoUrl(notificationvideoUrl);
				
				//�������ݵ����ݿ�
				history.save();


                Intent intent = new Intent(Constants.ACTION_SHOW_NOTIFICATION);
                intent.putExtra(Constants.NOTIFICATION_ID, notificationId);
                intent.putExtra(Constants.NOTIFICATION_API_KEY,
                        notificationApiKey);
                intent
                        .putExtra(Constants.NOTIFICATION_TITLE,
                                notificationTitle);
                intent.putExtra(Constants.NOTIFICATION_MESSAGE,
                        notificationMessage);
                intent.putExtra(Constants.NOTIFICATION_URI, notificationUri);
				intent.putExtra(Constants.NOTIFICATION_IMAGE_URI,
						notificationimageUri);
                intent.putExtra(Constants.NOTIFICATION_VIDEO_URI , notificationvideoUrl);

                //                intent.setData(Uri.parse((new StringBuilder(
                //                        "notif://notification.androidpn.org/")).append(
                //                        notificationApiKey).append("/").append(
                //                        System.currentTimeMillis()).toString()));

                xmppManager.getContext().sendBroadcast(intent);
                /***
                 * ZHANG 20160901 ADD ��������˷�����Ϣ��ִ
                 * ********/
                DeliverConfirmIQ deliverconfirmIQ = new DeliverConfirmIQ();
                deliverconfirmIQ.setUuid(notificationId);
                deliverconfirmIQ.setType(IQ.Type.SET);
                xmppManager.getConnection().sendPacket(deliverconfirmIQ);


            }
        }

    }

}
