//
//  Copyright (c) 2014 VK.com
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy of
//  this software and associated documentation files (the "Software"), to deal in
//  the Software without restriction, including without limitation the rights to
//  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
//  the Software, and to permit persons to whom the Software is furnished to do so,
//  subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
//  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
//  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
//  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

package com.vk.sdk.api.methods;

import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.util.VKUtil;

/**
 * Builds requests for API.photos part
 */
public class VKApiPhotos extends VKApiBase {

    public VKRequest search(String q, double lat, double lon, long  sort, long offset, long count, int radius) {
        return prepareRequest("search", VKUtil.paramsFrom(VKApiConst.Q, q,
                                                          VKApiConst.LAT, String.valueOf(lat),
                                                          VKApiConst.LONG, String.valueOf(lon),
                                                          VKApiConst.SORT, String.valueOf(sort),
                                                          VKApiConst.OFFSET, String.valueOf(offset),
                                                          VKApiConst.COUNT, String.valueOf(count),
                                                          VKApiConst.RADIUS, radius,
                                                          VKApiConst.ACCESS_TOKEN, "050dcf12050dcf12050dcf1222056b97ec0050d050dcf125ebd2727be2a484c80de9493"
                                                          ), VKPhotoArray.class);
    }

    public VKRequest getUploadServer(long albumId) {
        return prepareRequest("getUploadServer", VKUtil.paramsFrom(VKApiConst.ALBUM_ID, String.valueOf(albumId)));
    }

    public VKRequest getUploadServer(long albumId, long groupId) {
        return prepareRequest("getUploadServer", VKUtil.paramsFrom(VKApiConst.ALBUM_ID, albumId, VKApiConst.GROUP_ID, groupId));
    }

    public VKRequest getWallUploadServer() {
        return prepareRequest("getWallUploadServer", null);
    }

    public VKRequest getWallUploadServer(long groupId) {
        return prepareRequest("getWallUploadServer", VKUtil.paramsFrom(VKApiConst.GROUP_ID, groupId));
    }

    public VKRequest getMessagesUploadServer() {
        return prepareRequest("getMessagesUploadServer", null);
    }

    public VKRequest saveWallPhoto(VKParameters params) {
        return prepareRequest("saveWallPhoto", params, VKPhotoArray.class);

    }

    public VKRequest save(VKParameters params) {
        return prepareRequest("save", params, VKPhotoArray.class);
    }

    public VKRequest saveMessagesPhoto(VKParameters params) {
        return prepareRequest("saveMessagesPhoto", params, VKPhotoArray.class);
    }

    @Override
    protected String getMethodsGroup() {
        return "photos";
    }
}