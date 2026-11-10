package org.fuusio.flx.app.license

/**
 * Enumeration of Open Source Initiative (OSI) approved and other common open-source licenses.
 *
 * This enum provides access to standardized license metadata, including display names,
 * SPDX identifiers, and official license URLs.
 *
 * @property displayName The full, human-readable name of the license.
 * @property spdxId The short-form identifier as defined by the Software Package Data Exchange (SPDX).
 * @property url The official URL pointing to the license terms on opensource.org or the license's primary site.
 * @property licenseText A string containing the standard legal text or a summary of the license requirements.
 */
enum class OSILicense(val displayName: String, @Suppress("unused") val spdxId: String, val url: String) {
    APACHE_2("Apache License 2.0", "Apache-2.0", "https://opensource.org/license/apache-2-0"),
    BSD_1("BSD 1-Clause License", "BSD-1-Clause", "https://opensource.org/licenses/BSD-1-Clause"),
    BSD_2("BSD 2-Clause License", "BSD-2-Clause", "https://opensource.org/licenses/BSD-2-Clause"),
    BSD_3("BSD 3-Clause License", "BSD-3-Clause", "https://opensource.org/licenses/BSD-3-Clause"),
    ZERO_BSD("BSD Zero Clause License", "0BSD", "https://opensource.org/license/0bsd"),
    ECLIPSE_2(
        "Eclipse Public License Version 2.0",
        "EPL-2.0",
        "https://opensource.org/license/epl-2-0"
    ),
    MIT("MIT License", "MIT", "https://opensource.org/licenses/MIT"),
    MIT_0("MIT No Attribution License", "MIT-0", "https://opensource.org/licenses/mit-0"),
    MPL_2("Mozilla Public License 2.0", "MPL-2.0", "https://opensource.org/licenses/MPL-2.0"),
    SIL("SIL Open Font License 1.1", "SIL-OpenFont-1.1", "https://openfontlicense.org/open-font-license-official-text/");

    val licenseText: String get() =
        when (this) {
            APACHE_2 -> """
            Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
            """.trimIndent()
            BSD_1 -> """
                
            """.trimIndent()
            BSD_2 -> """
            Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

            1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

            2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

            THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
            """.trimIndent()
            BSD_3 -> """
            Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

            1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

            2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

            3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

            THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
            """.trimIndent()
            ECLIPSE_2 -> """
            The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License is available at http://www.eclipse.org/org/documents/edl-v10.php.
            """.trimIndent()
            MIT -> """
            Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

            The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
            
            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.    
            """.trimIndent()
            ZERO_BSD -> """
            Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted.

            THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
            """.trimIndent()
            MIT_0 -> """
                
            """.trimIndent()
            MPL_2 -> """
            This Source Code Form is subject to the terms of the Mozilla Public License, Version 2.0. If a copy of the MPL was not distributed with this software, You can obtain one at https://mozilla.org/MPL/2.0/.

            Covered Software is provided under this License on an "as is" basis, without warranty of any kind, either expressed, implied, or statutory, including, without limitation, warranties that the Covered Software is free of defects, merchantable, fit for a particular purpose or non-infringing.

            The MPL is a file-level copyleft licence: a work combining Covered Software with other material may be distributed under terms of your choice, provided the Covered Software itself, including any modifications to it, remains under this License and its Source Code Form is made available to recipients.

            The full license text is available at https://opensource.org/licenses/MPL-2.0
            """.trimIndent()
            SIL -> """
            Permission is hereby granted, free of charge, to any person obtaining a copy of the Font Software, to use, study, copy, merge, embed, modify, redistribute, and sell modified and unmodified copies of the Font Software, subject to the following conditions: 
            
            1. Neither the Font Software nor any of its individual components, in Original or Modified Versions, may be sold by itself.
            
            2. Original or Modified Versions of the Font Software may be bundled, redistributed and/or sold with any software, provided that each copy contains the above copyright notice and this license. These can be included either as stand-alone text files, human-readable headers or in the appropriate machine-readable metadata fields within text or binary files as long as those fields can be easily viewed by the user.
            
            3. No Modified Version of the Font Software may use the Reserved Font Name(s) unless explicit written permission is granted by the corresponding Copyright Holder. This restriction only applies to the primary font name as presented to the users.
            
            4. The name(s) of the Copyright Holder(s) or the Author(s) of the Font Software shall not be used to promote, endorse or advertise any Modified Version, except to acknowledge the contribution(s) of the Copyright Holder(s) and the Author(s) or with their explicit written permission.
            
            5. The Font Software, modified or unmodified, in part or in whole, must be distributed entirely under this license, and must not be distributed under any other license. The requirement for fonts to remain under this license does not apply to any document created using the Font Software.
            
            The full license the text is available in https://openfontlicense.org/open-font-license-official-text/
            """.trimIndent()
        }
}
