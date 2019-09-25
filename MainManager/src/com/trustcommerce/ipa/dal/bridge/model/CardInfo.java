package com.trustcommerce.ipa.dal.bridge.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardInfo {
	
	  /** log4j logger.*/
    private static final Logger LOGGER = LoggerFactory.getLogger(CardInfo.class);

    public enum CardType {
        UNKNOWN, credit, debit
    };

    private String aid;
    private boolean cashback;
    private CardType cardType;
    private boolean masterCard;
    private String tracksData;

  
    
    /**
     * 
     * @param aid String
     * @param cashback boolean
     * @param cType CardType
     * @param masterCard boolean
     * @param trackData String
     */
    public CardInfo(final String aid, boolean cashback, final CardType cType, boolean masterCard,
            final String trackData) {
        this.aid = aid;
        this.cashback = cashback;
        this.cardType = cType;
        this.masterCard = masterCard;
        this.tracksData = trackData;
        LOGGER.debug("aid = {}, cashback= {}, cardType= {}, masterCard= {} ", this.aid, this.cashback, this.cardType, 
                this.masterCard);
    }
    /**
     * 
     * @return String
     */
    
    public final String getAid() {
        return aid;
    }
    /**
     * 
     * @return boolean
     */
    public final boolean isCashbackAllowed() {
        return cashback;
    }
    /**
     * 
     * @return CardType
     */
    public final CardType getCardType() {
        return cardType;
    }
    /**
     * 
     * @return boolean
     */
    public final boolean isMasterCard() {
        return masterCard;
    }
    /**
     * 
     * @return String
     */
    public final String getTracksData() {
        return tracksData;
    }
}
