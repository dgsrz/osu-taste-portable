/*
 * Copyright (C) 2014 dgsrz Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package net.moesky.osuplayer.beatmap;

/**
 * @author dgsrz(dgsrz@vip.qq.com)
 */
public enum HitObjectType {

    Normal(1), Slider(2), Spinner(8);

    private int value;

    private HitObjectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
