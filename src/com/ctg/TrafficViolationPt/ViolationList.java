package com.ctg.TrafficViolationPt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChaLi on 12/26/2014.
 */
public class ViolationList {
    private List<ViolationPt> mlsPt = null;

    public ViolationList(){
        mlsPt = new ArrayList<ViolationPt>();
    }

    public List<ViolationPt> getMlsPt() {
        return mlsPt;
    }

    public void addPt(ViolationPt pt){
        mlsPt.add(pt);
    }

    public void clearPtList(){
        if (mlsPt.size()>0){
            mlsPt.clear();
        }
    }
}
