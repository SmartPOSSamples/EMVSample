package com.smartpos.EMVKernel;

public class EMVInitParam
{
	/**
	 * PBOC processing: 1; QPBOC processing: 2; Electronic cash processing: 3
	 */
	public int TransType = 0;
	
	/**
	 * Card reader handle
	 */
	public int ReaderHandle = 0;
	
	/**
	 * CARD_CONTACT:1 ; CARD_CONTACTLESS:2
	 */
	public byte CardType;
	
	public short ATRLength;
	
	public byte[] ATR = new byte[30];
}
