package lht.trash;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class pic_searchFragment extends Fragment {

    public pic_searchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pic_search, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //getView().findViewById(R.id.bt_pic_to_text).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_pic_searchFragment_to_text_searchFragment));
        //getView().findViewById(R.id.bt_pic_to_voice).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_pic_searchFragment_to_voice_searchFragment));
    }
}
