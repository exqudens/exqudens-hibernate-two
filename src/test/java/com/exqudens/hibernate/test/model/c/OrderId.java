package com.exqudens.hibernate.test.model.c;

import java.io.Serializable;

import javax.persistence.Embeddable;

import groovy.transform.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@Embeddable
public class OrderId implements Serializable {

    private static final long serialVersionUID = -2900730569723152028L;

    private Long id;
    private String name;
    private String number;

}
