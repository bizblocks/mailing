package com.lokoproject.mailing.notification.template.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Antonlomako. created on 01.01.2019.
 */
public class Table extends AbstractTemplateElement {

    private TableRow header;

    private List<TableRow> rows=new ArrayList<>();

    public TableRow getHeader() {
        return header;
    }

    public void setHeader(TableRow header) {
        this.header = header;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }
}
