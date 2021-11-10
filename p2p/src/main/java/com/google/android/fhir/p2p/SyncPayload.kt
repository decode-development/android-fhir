/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.fhir.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.hl7.fhir.r4.model.ResourceType

/** The payload for syncing changes between peers. */
@Serializable
data class SyncPayload(
  val resourceId: String,
  val resourceType: ResourceType,
  val lastUpdatedDate: String,
  val serializedResource: String,
  /**
   * The list of changes which have been made to the resource since it was last updated from the
   * server.
   */
  val changes: List<RemoteChange>,
)

/** A change to a resource which was made by another client. */
@Serializable
sealed class RemoteChange {
  /** The timestamp at which the change took place. */
  abstract val timestamp: String

  /** The creation of a new resource locally on the client. */
  @Serializable
  @SerialName("create")
  data class Create(
    override val timestamp: String,
    val resource: String
  ) : RemoteChange()

  /** An update to a resource. */
  @Serializable
  @SerialName("update")
  data class Update(
    override val timestamp: String,
    val patch: String
  ) : RemoteChange()

  /** The deletion of a resource. */
  @Serializable
  @SerialName("delete")
  data class Delete(
    override val timestamp: String
  ) : RemoteChange()
}
