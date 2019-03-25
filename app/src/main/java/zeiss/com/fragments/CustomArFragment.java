package zeiss.com.fragments;
import android.util.Log;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;
import zeiss.com.ARActivity;

public class CustomArFragment extends ArFragment {
    @Override
    protected Config getSessionConfiguration(Session session) {
        getPlaneDiscoveryController().setInstructionView(null);
        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        getArSceneView().setupSession(session);

        if ((((ARActivity) getActivity()).setupAugmentedImagesDb(config, session))) {
            Log.d("SetupAugImgDb", "Success");
        } else {
            Log.e("SetupAugImgDb","Failure setting up db");
        }

        return config;
    }
}
