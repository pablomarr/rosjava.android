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

package org.ros.android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import geometry_msgs.PoseStamped;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class OrientationPublisher implements NodeMain {

  private final SensorManager sensorManager;

  private OrientationListener orientationListener;

  private final class OrientationListener implements SensorEventListener {

    private final Publisher<geometry_msgs.PoseStamped> publisher;

    private OrientationListener(Publisher<geometry_msgs.PoseStamped> publisher) {
      this.publisher = publisher;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
        float[] quaternion = new float[4];
        SensorManager.getQuaternionFromVector(quaternion, event.values);
        PoseStamped pose = publisher.newMessage();
        pose.header().frame_id("/map");
        // TODO(damonkohler): Should get time from the Node.
        pose.header().stamp(Time.fromMillis(System.currentTimeMillis()));
        pose.pose().orientation().w(quaternion[0]);
        pose.pose().orientation().x(quaternion[1]);
        pose.pose().orientation().y(quaternion[2]);
        pose.pose().orientation().z(quaternion[3]);
        publisher.publish(pose);
      }
    }
  }

  public OrientationPublisher(SensorManager sensorManager) {
    this.sensorManager = sensorManager;
  }

  @Override
  public GraphName getDefaultNodeName() {
    return new GraphName("android/orientiation_sensor");
  }

  @Override
  public void onStart(Node node) {
    try {
      Publisher<geometry_msgs.PoseStamped> publisher =
          node.newPublisher("android/orientation", "geometry_msgs/PoseStamped");
      orientationListener = new OrientationListener(publisher);
      Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
      // 10 Hz
      sensorManager.registerListener(orientationListener, sensor, 500000);
    } catch (Exception e) {
      node.getLog().fatal(e);
    }
  }

  @Override
  public void onShutdown(Node node) {
  }

  @Override
  public void onShutdownComplete(Node node) {
  }
}
