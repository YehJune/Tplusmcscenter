package com.kct.tplusmcscenter.network.dto.widget.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResponseWidgetBody(

    /**
     * @param traceno keyfile
     */
    @SerializedName("traceno")
    @Expose
    val traceno: String,

    /**
     * @param mdn String
     */
    @SerializedName("mdn")
    @Expose
    val mdn: String,

    /**
     * @param sms String, Unit: 건
     */
    @SerializedName("sms")
    @Expose
    val sms: String,

    /**
     * @param voice String, Unit: 분
     */
    @SerializedName("voice")
    @Expose
    val voice: String,

    /**
     * @param data String, Unit: GB
     */
    @SerializedName("data")
    @Expose
    val data: String,

    /**
     * @param smspercent Int, Unit: %
     */
    @SerializedName("smspercent")
    @Expose
    val smspercent: Int,

    /**
     * @param voicepercent Int, Unit: %
     */
    @SerializedName("voicepercent")
    @Expose
    val voicepercent: Int,

    /**
     * @param datapercent Int, Unit: %
     */
    @SerializedName("datapercent")
    @Expose
    val datapercent: Int,

    /**
     * @param smstot String, Unit: 건
     */
    @SerializedName("smstot")
    @Expose
    val smstot: String,

    /**
     * @param voicetot String, Unit: 분
     */
    @SerializedName("voicetot")
    @Expose
    val voicetot: String,

    /**
     * @param datatot String, Unit: GB
     */
    @SerializedName("datatot")
    @Expose
    val datatot: String,








)
