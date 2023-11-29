package edu.sumdu.tss.elephant.helperunits.enums;

import edu.sumdu.tss.elephant.helper.enums.Lang;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LangTest {

    @Test
    public void EnbyLangTest(){
        Lang res=Lang.byValue("EN");
        assertEquals(res,Lang.EN);

    }

}
