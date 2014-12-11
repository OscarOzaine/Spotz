/*
 * Copyright 2002-2014 Drew Noakes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */

package com.spotz.utils.imaging.psd;

import com.spotz.utils.lang.RandomAccessFileReader;
import com.spotz.utils.lang.RandomAccessStreamReader;
import com.spotz.utils.lang.annotations.NotNull;
import com.spotz.utils.metadata.Metadata;
import com.spotz.utils.metadata.photoshop.PsdReader;

import java.io.*;

/**
 * Obtains metadata from Photoshop's PSD files.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class PsdMetadataReader
{
    @NotNull
    public static Metadata readMetadata(@NotNull File file) throws IOException
    {
        Metadata metadata = new Metadata();

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

        try {
            new PsdReader().extract(new RandomAccessFileReader(randomAccessFile), metadata);
        } finally {
            randomAccessFile.close();
        }

        return metadata;
    }

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream)
    {
        Metadata metadata = new Metadata();
        new PsdReader().extract(new RandomAccessStreamReader(inputStream), metadata);
        return metadata;
    }
}
