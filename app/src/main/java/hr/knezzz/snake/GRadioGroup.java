package hr.knezzz.snake;

/**
 * Created on 14/06/14.
 */
import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class GRadioGroup {

    List<RadioButton> radios = new ArrayList<RadioButton>();

    /**
     * Constructor, which allows you to pass number of RadioButton instances,
     * making a group.
     *
     * @param radios
     *            One RadioButton or more.
     */
    public GRadioGroup(RadioButton... radios) {
        super();

        for (RadioButton rb : radios) {
            this.radios.add(rb);
            rb.setOnClickListener(onClick);
        }
    }

    /**
     * Get checked radioButton
     */
    public int getSelected(){
        for(RadioButton rb:radios){
            if(rb.isChecked())
                return radios.indexOf(rb);
        }

        return -1;
    }

    /**
     * set custom radio button on. Used when fetching data from settings.
     * @param i - number of radio button
     */
    public void setSelected(int i){
        for(RadioButton rb:radios){
            ViewParent p = rb.getParent();
            if (p.getClass().equals(RadioGroup.class)) {
                // if RadioButton belongs to RadioGroup,
                // then deselect all radios in it
                RadioGroup rg = (RadioGroup) p;
                rg.clearCheck();
            } else {
                // if RadioButton DOES NOT belong to RadioGroup,
                // just deselect it
                rb.setChecked(false);
            }
        }
        radios.get(i).setChecked(true);
    }

    /**
     * This occurs everytime when one of RadioButtons is clicked,
     * and deselects all others in the group.
     */
    OnClickListener onClick = new OnClickListener() {

        @Override
        public void onClick(View v) {

            // let's deselect all radios in group
            for (RadioButton rb : radios) {

                ViewParent p = rb.getParent();
                if (p.getClass().equals(RadioGroup.class)) {
                    // if RadioButton belongs to RadioGroup,
                    // then deselect all radios in it
                    RadioGroup rg = (RadioGroup) p;
                    rg.clearCheck();
                } else {
                    // if RadioButton DOES NOT belong to RadioGroup,
                    // just deselect it
                    rb.setChecked(false);
                }
            }

            // now let's select currently clicked RadioButton
            if (v.getClass().equals(RadioButton.class)) {
                RadioButton rb = (RadioButton) v;
                rb.setChecked(true);
            }

        }
    };

}