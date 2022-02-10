package de.hse.jodel.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;


@Entity
@Table(name = "channels")
public class Channel extends PanacheEntityBase {

    @Id
    @TableGenerator(name = "channelSeq", table = "sequence", pkColumnName = "seq_name",
            pkColumnValue = "channels", valueColumnName = "seq_count", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "channelSeq")
    @Column(name = "id")
    public Long id;

    @Column(name = "name", length = 45)
    public String name;

    @Column(name = "info", length = 100)
    public String info;

    @Column(name = "symbol", length = 20)
    public String symbol;
}