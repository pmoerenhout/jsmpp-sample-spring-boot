/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.jsmpp.sample.springboot.jsmpp;

import org.jsmpp.extra.SessionState;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionStateListenerImpl implements SessionStateListener {

  private static final Logger LOG = LoggerFactory.getLogger(SessionStateListenerImpl.class);

  public void onStateChange(SessionState newState, SessionState oldState, Session source) {
    LOG.info("Session {} changed from {} to {}", source.getSessionId(), oldState, newState);
  }
}

