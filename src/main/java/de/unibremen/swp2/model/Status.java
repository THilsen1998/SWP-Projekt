package de.unibremen.swp2.model;

import lombok.Data;

/**
 * @Author Martin
 *Association class between @link #participant} and {@link #meeting}
 *
 *  */
@Data
public class Status
{
    /**
     * The meeting
     */
    private Meeting meeting;
    /**
     * The participant
     */
    private Participant participant;


}
