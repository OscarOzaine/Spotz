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
package com.spotz.utils.metadata.jfif;

import com.spotz.utils.lang.annotations.NotNull;
import com.spotz.utils.metadata.Directory;
import com.spotz.utils.metadata.MetadataException;

import java.util.HashMap;

/**
 * Directory of tags and values for the SOF0 Jfif segment.  This segment holds basic metadata about the image.
 *
 * @author Yuri Binev, Drew Noakes
 */
public class JfifDirectory extends Directory
{
    public static final int TAG_VERSION = 5;
    /** Units for pixel density fields.  One of None, Pixels per Inch, Pixels per Centimetre. */
    public static final int TAG_UNITS = 7;
    public static final int TAG_RESX = 8;
    public static final int TAG_RESY = 10;

    @NotNull
    protected static final HashMap<Integer, String> _tagNameMap = new HashMap<Integer, String>();

    static
    {
        _tagNameMap.put(TAG_VERSION, "Version");
        _tagNameMap.put(TAG_UNITS, "Resolution Units");
        _tagNameMap.put(TAG_RESY, "Y Resolution");
        _tagNameMap.put(TAG_RESX, "X Resolution");
    }

    public JfifDirectory()
    {
        this.setDescriptor(new JfifDescriptor(this));
    }

    @Override
    @NotNull
    public String getName()
    {
        return "JFIF";
    }

    @Override
    @NotNull
    protected HashMap<Integer, String> getTagNameMap()
    {
        return _tagNameMap;
    }

    public int getVersion() throws MetadataException
    {
        return getInt(JfifDirectory.TAG_VERSION);
    }

    public int getResUnits() throws MetadataException
    {
        return getInt(JfifDirectory.TAG_UNITS);
    }

    public int getImageWidth() throws MetadataException
    {
        return getInt(JfifDirectory.TAG_RESY);
    }

    public int getImageHeight() throws MetadataException
    {
        return getInt(JfifDirectory.TAG_RESX);
    }

}
