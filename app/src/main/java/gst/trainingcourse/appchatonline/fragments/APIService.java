package gst.trainingcourse.appchatonline.fragments;

import gst.trainingcourse.appchatonline.notification.MyResponse;
import gst.trainingcourse.appchatonline.notification.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA38iU2ZI:APA91bHCq5jE8hCNMn8BvmdUPV-8IX2hnBqpDOPbOj78AQzIe3viBZ_n-OHwJCv9JEZpy6o9IY8cA9Ri7jJpq1qMDg0zadzAVMq3CUTRIzG11oTrrI_O-o8Hs9N8rB9ljUZM3i1D-QMi"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
