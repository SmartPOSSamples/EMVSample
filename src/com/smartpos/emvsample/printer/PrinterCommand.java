package com.smartpos.emvsample.printer;

public class PrinterCommand
{
    /**
     * Print the data in the printer buffer, then feed paper for one line according to the current line space settings.
     * After printing, the print position moves to the beginning of the line.
     *
     * @return
     */
    static public byte[] linefeed()
    {
        return new byte[] { (byte) 0x0A };
    }

    /**
     * The printing position jumps to the next tab stop, which is the starting position of 8 characters.
     *
     * @return
     */
    static public byte[] getCmdHt()
    {
        return new byte[] { (byte) 0x09 };
    }

    /**
     * The data in the print buffer, if there is a black mark function, will feed the paper to the next black mark position after printing.
     *
     * @return
     */
    static public byte[] getCmdFf()
    {
        return new byte[] { (byte) 0x0c };
    }

    /**
     *
     * Print the contents of the line buffer and advance the paper n lines.
     * This command is only valid for this line and does not change the line spacing value set by the ESC 2 and ESC 3 commands.
     *
     * @param n
     *            0-255
     * @return
     */
    static public byte[] getCmdEscJN(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x4A, (byte) n };
    }

    /**
     * The data in the print buffer, if there is a black mark function, will feed the paper to the next black mark position after printing.
     *
     * @return
     */
    static public byte[] getCmdEscFf()
    {
        return new byte[] { (byte) 0x1b, (byte) 0x0c };
    }

    /**
     * Print the contents of the line buffer and advance the paper n lines.
     * The row height is the value set by ESC 2 and ESC 3
     *
     * @param n
     *            0-255
     * @return
     */
    static public byte[] feedLine(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x64, (byte) n };
    }

    /**
     * 1: The printer is in online mode, accepts print data and prints
     * 0: The printer is in offline mode, does not accept print data
     *
     * @param n
     *            :0,1 The lowest bit is valid
     * @return
     */
    static public byte[] getCmdEscN(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x3d, (byte) n };
    }

    /*--------------------------Line spacing setting commands-----------------------------*/

    /**
     * Set line spacing to 4 mm, 32 points
     *
     * @return
     */
    static public byte[] getCmdEsc2()
    {
        return new byte[] { (byte) 0x1B, (byte) 0x32 };
    }

    /**
     * Set line spacing to n points lines. The default line spacing is 32 points.
     *
     * @param n
     *            :0-255
     * @return
     */
    static public byte[] getCmdEsc3N(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x33, (byte) n };
    }

    /**
     * Set the alignment of print lines, default: left-aligned 0 ≤ n ≤ 2 or 48 ≤ n ≤ 50 left-aligned: n=0,48
     *      * Center alignment: n=1,49
     *      * Right justified: n=2,50
     *
     * @param n
     *            :0 ≤ n ≤ 2 or 48 ≤ n ≤ 50
     * @return
     */
    static public byte[] setAlignMode(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x61, (byte) n };
    }

    /**
     * Set the left margin for printing, the default is 0. The left margin is nL+nH*256, unit 0.125mm
     *
     * @param nL
     * @param nH
     * @return
     */
    static public byte[] getCmdGsLNlNh(int nL, int nH)
    {
        return new byte[] { (byte) 0x1D, (byte) 0x4c, (byte) nL, (byte) nH };
    }

    /**
     * Set the left margin of printing, the default is 0, the left margin is nL+nH*256, the unit is 0.125mm
     *
     * @param nL
     * @param nH
     * @return
     */
    static public byte[] getCmdEsc$NlNh(int nL, int nH)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x24, (byte) nL, (byte) nH };
    }

    /*--------------------------Character setting commands-----------------------------*/

    /**
     * Used to set the way characters are printed. Default value is 0
     *
     * @param n
     * Bit 0: Reserved Bit 1:1: Inverse font Bit 2:1: Inverted font Bit 3:1: Bold font Bit 4:1: Double height bit
     *      5:1: double width bit 6:1: strikethrough
     * @return
     */
    static public byte[] setFont(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x21, (byte) n };
    }

    /**
     * The lower 4 bits of n indicate whether the height is enlarged, equal to 0 means not enlarged.
     * The high 4 bits of n indicate whether the width is enlarged, equal to 0 means not enlarged.
     *
     * @param n
     * @return
     */
    static public byte[] setFontEnlarge(int n)
    {
        return new byte[] { (byte) 0x1D, (byte) 0x21, (byte) n };
    }

    /**
     * When equal to 0, cancel the font boldness. When it is not 0, set the font boldness.
     *
     * @param n
     *            The lowest bit is valid
     * @return
     */
    static public byte[] setFontBold(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x45, (byte) n };
    }

    /**
     * Default value:0
     *
     * @param n
     *            : represents the spacing between two characters
     * @return
     */
    static public byte[] setFontDistance(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x20, (byte) n };
    }

    /**
     * All characters after this command are printed at 2 times the normal width;
     * this command can be deleted with Enter or DC4 command.
     *
     * @return
     */
    static public byte[] getCmdEscSo()
    {
        return new byte[] { (byte) 0x1B, (byte) 0x0E };
    }

    /**
     * After the command is executed, the characters return to normal width printing.
     *
     * @return
     */
    static public byte[] getCmdEscDc4()
    {
        return new byte[] { (byte) 0x1B, (byte) 0x14 };
    }

    /**
     * Default:0
     *
     * @param n
     *            n=1: Set characters to be inverted n=0: Cancel characters to be inverted
     * @return
     */
    static public byte[] getCmdEsc__N(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x7B, (byte) n };
    }

    /**
     * Default:0
     *
     * @param n
     *            n=1: Set characters to be printed in reverse n=0: Cancel characters to be printed in reverse
     * @return
     */
    static public byte[] getCmdGsBN(int n)
    {
        return new byte[] { (byte) 0x1D, (byte) 0x42, (byte) n };
    }

    /**
     * Default:0
     *
     * @param n
     *            n=0-2, underline height
     * @return
     */
    static public byte[] getCmdEsc___N(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x2D, (byte) n };
    }

    /**
     *
     * @param n
     *            n=1: Select user-defined character set; n=0: Select internal character set (default)
     * @return
     */
    static public byte[] getCmdEsc____N(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x25, (byte) n };
    }

    /**
     * Used to set user-defined characters, up to 32 user-defined characters can be set.
     *
     * @return
     */
    static public byte[] getCmdEsc_SNMW()
    {
        return null;
    }

    /**
     * The command is used to cancel user-defined characters.
     * After the characters are canceled, the system characters will be used.
     *
     * @param n
     * @return
     */
    static public byte[] getCmdEsc_____N(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x25, (byte) n };
    }

    /**
     * Select an international character set. The Chinese version does not support this command.
     *
     * @param n
     *            The international character set settings are as follows:
     *            0:USA 1:France 2:Germany 3:U.K. 4:Denmark 1 5:Sweden
     *            6:Italy 7:Spain1 8:Japan 9:Norway 10:Denmark II 11:Spain II
     *            12:Latin America 13:Korea
     * @return
     */
    static public byte[] getCmdEscRN(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x52, (byte) n };
    }

    /**
     * Select the character code page. The character code page is used to select printing characters from 0x80~0xfe.
     * The Chinese version does not support this command
     *
     * @param n
     *            The character code page parameters are as follows: 0:437 1:850
     * @return
     */
    static public byte[] getCmdEscTN(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x74, (byte) n };
    }

    /*--------------------------Graphic printing commands omitted-----------------------------*/

    /*--------------------------Key control commands-----------------------------*/

    /**
     * Allow/disable key switch command. This command is not supported for the time being.
     *
     * @param n
     *            n=1, disable key pressing n=0, allow key pressing (default)
     * @return
     */
    static public byte[] getCmdEscC5N(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x63, (byte) 0x35, (byte) n };
    }

    /*--------------------------initialization commands-----------------------------*/

    /**
     * Initialize the printer. Clear the print buffer. Restore default values.
     * Select character printing method. Delete user-defined characters
     *
     * @return
     */
    static public byte[] init()
    {
        return new byte[] { (byte) 0x1B, (byte) 0x40 };
    }

    /*--------------------------status transfer commands-----------------------------*/

    /**
     * Communicate control board status to host
     *
     * @param n
     * @return
     */
    static public byte[] getCmdEscVN(int n)
    {
        return new byte[] { (byte) 0x1B, (byte) 0x76, (byte) n };
    }

    /**
     * When valid, the printer detects a status change and automatically sends the status to the host.
     * Please refer to the ESC/POS command level for details.
     *
     * @param n
     * @return
     */
    static public byte[] getCmdGsAN(int n)
    {
        return new byte[] { (byte) 1D, (byte) 61, (byte) n };
    }

    /**
     * Transmit peripheral device status to the host, only valid for serial port printers.
     * This command is not supported. Please refer to the ESC/POS command set for details.
     *
     * @param n
     * @return
     */
    static public byte[] getCmdEscUN(int n)
    {
        return new byte[]{ (byte) 0x1B, (byte) 0x75, (byte) n };
    }

    static public byte[] setHeatTime(int n)
    {
        return new byte[]{ 0x1B, 0x37, 7,(byte)n, 2};
    }

    /*--------------------------Barcode printing commands omitted-----------------------------*/

    /*--------------------------Control panel parameter commands omitted-----------------------------*/

    /**
     * Custom tab stops (2 spaces)
     *
     * @return
     */
    static public byte[] getCustomTabs()
    {
        return "  ".getBytes();
    }


}