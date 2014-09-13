package io.perihelion.fitfactor;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.PushService;

/**
 * Created by vincente on 9/12/14
 */
public class FitFactor extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "KqI3fIwrmgp1rep6UX31wZipcJACRJwtG66GNYoV", "fwXo7e2YQogv0OQybIhJqYHmsIVmEWZWNI92nbg0");
        ParseFacebookUtils.initialize("687629864647249");
        PushService.setDefaultPushCallback(this, MainActivity.class);
    }
}
