/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.views;

import com.google.common.base.Preconditions;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import org.ros.message.Time;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;

import java.util.ArrayList;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class PublishingPreviewCallback implements PreviewCallback {

  private final Node node;
  private final Publisher<sensor_msgs.CompressedImage> imagePublisher;
  private final Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;

  public PublishingPreviewCallback(Node node,
      Publisher<sensor_msgs.CompressedImage> imagePublisher,
      Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher) {
    this.node = node;
    this.imagePublisher = imagePublisher;
    this.cameraInfoPublisher = cameraInfoPublisher;
  }

  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    Preconditions.checkNotNull(data);
    Preconditions.checkNotNull(camera);

    Time currentTime = node.getCurrentTime();
    String frameId = "camera";

    sensor_msgs.CompressedImage image = imagePublisher.newMessage();
    image.data(new ArrayList<Short>());
    for (byte b : data) {
      image.data().add((short) b);
    }
    image.format("jpeg");
    image.header().stamp(currentTime);
    image.header().frame_id(frameId);
    imagePublisher.publish(image);

    sensor_msgs.CameraInfo cameraInfo = cameraInfoPublisher.newMessage();
    cameraInfo.header().stamp(currentTime);
    cameraInfo.header().frame_id(frameId);

    Size previewSize = camera.getParameters().getPreviewSize();
    cameraInfo.width(previewSize.width);
    cameraInfo.height(previewSize.height);
    cameraInfoPublisher.publish(cameraInfo);
  }
}