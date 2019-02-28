package com.lokoproject.mailing.web.groovyscript;

import com.haulmont.cuba.gui.components.EntityCombinedScreen;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.lokoproject.mailing.entity.GroovyScript;

import javax.inject.Inject;
import java.util.Map;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.data.Datasource;

public class GroovyScriptBrowse extends EntityCombinedScreen {

    @Inject
    private SourceCodeEditor script;

    @Inject
    private Table<GroovyScript> table;

    @Inject
    private Datasource<GroovyScript> groovyScriptDs;

    

    @Override
    public void init(Map<String,Object> param){
        super.init(param);
        script.setEditable(false);

        table.setItemClickAction(new BaseAction("click"){
            @Override
            public void actionPerform(Component component){
               onEditBtnClick();
            }
        });

        groovyScriptDs.addItemChangeListener(event->{
            script.setValue(groovyScriptDs.getItem()==null?
                    null:
                    groovyScriptDs.getItem().getScript());
        });
    }

    public void onEditBtnClick() {
        script.setEditable(true);
        script.setValue(groovyScriptDs.getItem().getScript());
        table.getAction("edit").actionPerform(this);
    }

    public void onSaveBtnClick() {
        groovyScriptDs.getItem().setScript(script.getValue());
        script.setEditable(false);
        getAction("save").actionPerform(this);
    }

    public void onCancelBtnClick() {
        script.setEditable(false);
        getAction("cancel").actionPerform(this);
    }

    public void onCreateBtnClick() {
        script.setEditable(true);
        table.getAction("create").actionPerform(this);
    }


}