package com.spotz.utils.metadata.gif;

import com.spotz.utils.lang.annotations.NotNull;
import com.spotz.utils.metadata.TagDescriptor;

/**
 * @author Drew Noakes https://drewnoakes.com
 */
public class GifHeaderDescriptor extends TagDescriptor<GifHeaderDirectory>
{
    public GifHeaderDescriptor(@NotNull GifHeaderDirectory directory)
    {
        super(directory);
    }

//    @Override
//    public String getDescription(int tagType)
//    {
//        switch (tagType) {
//            case GifHeaderDirectory.TAG_COMPRESSION:
//                return getCompressionDescription();
//            default:
//                return super.getDescription(tagType);
//        }
//    }
}
