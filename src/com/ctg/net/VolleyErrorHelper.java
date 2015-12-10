package com.ctg.net;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public class VolleyErrorHelper {
	/**
     * Returns appropriate message which is to be displayed to the user
     * against the specified error object.
     *
     * @param error
     *
     * @return
     */
    public static String getMessage(Object error) {
        if (error instanceof TimeoutError) {
            return "连接超时";
        }
        else if (isServerProblem(error)) {
            return handleServerError(error);
        }
        else if (isNetworkProblem(error)) {
            return "网络出错啦";
        }
        return "";
    }

    /**
     * Determines whether the error is related to network
     * @param error
     * @return
     */
    private static boolean isNetworkProblem(Object error) {
        return (error instanceof NetworkError) || (error instanceof NoConnectionError);
    }
    /**
     * Determines whether the error is related to server
     * @param error
     * @return
     */
    private static boolean isServerProblem(Object error) {
        return (error instanceof ServerError) || (error instanceof AuthFailureError);
    }
    /**
     * Handles the server error, tries to determine whether to show a stock message or to
     * show a message retrieved from the server.
     *
     * @param err
     *
     * @return
     */
    private static String handleServerError(Object err) {
        VolleyError error = (VolleyError) err;

        NetworkResponse response = error.networkResponse;

        if (response != null) {
            return String.format("服务器出错:%d",response.statusCode);
        }
        return "未知服务器错误";
    }
}
