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

import ca.uhn.fhir.context.FhirContext
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ResourceType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(BlockJUnit4ClassRunner::class)
class SyncPayloadTest {
  @Test
  fun encodeToStringEncodesThePayloadToAJsonString() {
    val resourceId = "abc"
    val resourceType = ResourceType.Patient
    val lastUpdatedDate = Instant.now().toString()
    val resource = Patient().apply { id = resourceId }
    val serializedResource = FhirContext.forR4().newJsonParser().encodeResourceToString(resource)
    val patch = "[{\"op\":\"add\"}]"
    val createTimestamp = "2021-11-03"
    val updateTimestamp = "2021-11-04"
    val deleteTimestamp = "2021-11-05"

    val encoded =
      Json.encodeToString(
        SyncPayload(
          resourceId,
          resourceType,
          lastUpdatedDate,
          serializedResource,
          listOf(
            RemoteChange.Create(createTimestamp, serializedResource),
            RemoteChange.Update(updateTimestamp, patch),
            RemoteChange.Delete(deleteTimestamp),
          )
        )
      )

    assertThat(encoded)
      .isEqualTo(
        """{"resourceId":${Json.encodeToString(resourceId)},"resourceType":${Json.encodeToString(resourceType)},"lastUpdatedDate":${Json.encodeToString(lastUpdatedDate)},"serializedResource":${Json.encodeToString(serializedResource)},"changes":[{"type":"create","timestamp":${Json.encodeToString(createTimestamp)},"resource":${Json.encodeToString(serializedResource)}},{"type":"update","timestamp":${Json.encodeToString(updateTimestamp)},"patch":${Json.encodeToString(patch)}},{"type":"delete","timestamp":${Json.encodeToString(deleteTimestamp)}}]}"""
      )
  }

  @Test
  fun decodeFromStringDecodesThePayloadFromAJsonString() {
    val resourceId = "abc"
    val resourceType = ResourceType.Patient
    val lastUpdatedDate = Instant.now().toString()
    val resource = Patient().apply { id = resourceId }
    val serializedResource = FhirContext.forR4().newJsonParser().encodeResourceToString(resource)
    val patch = "[{\"op\":\"add\"}]"
    val createTimestamp = "2021-11-03"
    val updateTimestamp = "2021-11-04"
    val deleteTimestamp = "2021-11-05"
    val payload =
      """{"resourceId":${Json.encodeToString(resourceId)},"resourceType":${Json.encodeToString(resourceType)},"lastUpdatedDate":${Json.encodeToString(lastUpdatedDate)},"serializedResource":${Json.encodeToString(serializedResource)},"changes":[{"type":"create","timestamp":${Json.encodeToString(createTimestamp)},"resource":${Json.encodeToString(serializedResource)}},{"type":"update","timestamp":${Json.encodeToString(updateTimestamp)},"patch":${Json.encodeToString(patch)}},{"type":"delete","timestamp":${Json.encodeToString(deleteTimestamp)}}]}"""

    val decoded: SyncPayload = Json.decodeFromString(payload)

    assertThat(decoded.resourceId).isEqualTo(resourceId)
    assertThat(decoded.resourceType).isEqualTo(ResourceType.Patient)
    assertThat(decoded.lastUpdatedDate).isEqualTo(lastUpdatedDate)
    assertThat(decoded.serializedResource).isEqualTo(serializedResource)
    assertThat(decoded.changes)
      .isEqualTo(
        listOf(
          RemoteChange.Create(createTimestamp, serializedResource),
          RemoteChange.Update(updateTimestamp, patch),
          RemoteChange.Delete(deleteTimestamp),
        )
      )
  }
}
