/*
 * Copyright @ 2018 Atlassian Pty Ltd
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
 *
 */

package org.jitsi.jibri.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Read from an infinite [InputStream] and make available the most
 * recent line that was read via [mostRecentLine]. NOTE: this class
 * will not read from the stream automatically, its [readLine]
 * method must be called.
 */
class TailLogic(inputStream: InputStream) {
    private val reader = BufferedReader(InputStreamReader(inputStream))
    @Volatile var mostRecentLine: String = ""

    fun readLine() {
        mostRecentLine = reader.readLine()
    }
}

/**
 * A wrapper around [TailLogic] which Spins up a thread to constantly
 * read from the given [InputStream] and save the most-recently-read
 * line as [mostRecentLine] to be read by whomever is interested.
 */
class Tail(inputStream: InputStream) {
    private val tailLogic = TailLogic(inputStream)
    private val executor = Executors.newSingleThreadExecutor(NameableThreadFactory("Tail"))
    private var task: Future<*>
    var mostRecentLine: String = ""
        get() {
            return tailLogic.mostRecentLine
        }

    init {
        task = executor.submit {
            while (true) {
                tailLogic.readLine()
            }
        }
    }

    fun stop() {
        task.cancel(true)
    }
}
