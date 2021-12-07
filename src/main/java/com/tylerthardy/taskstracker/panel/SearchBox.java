package com.tylerthardy.taskstracker.panel;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SearchBox extends JTextField
{
    private SearchBoxCallback fn;

    public SearchBox()
    {
        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                fn.call();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                fn.call();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
            }
        });

        this.addActionListener(e -> fn.call()
        );
    }

    public void addTextChangedListener(SearchBoxCallback fn) {
        this.fn = fn;
    }

    public interface SearchBoxCallback {
        void call();
    }
}
