package tech.algofinserve.recommendation.alerts.persistance;

import tech.algofinserve.recommendation.constants.BuySell;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="stock_code", nullable=false)
    private String stockCode;

    private String price;

    @Column(name="alert_date", nullable=false)
    private Instant alertDate;

    private String scanName;

    @Enumerated(EnumType.STRING)
    private BuySell buySell;

    @Column(name="since_days")
    private Integer sinceDays = 0;

    private Instant createdAt = Instant.now();

    public AlertEntity() {}

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public Instant getAlertDate() { return alertDate; }
    public void setAlertDate(Instant alertDate) { this.alertDate = alertDate; }
    public String getScanName() { return scanName; }
    public void setScanName(String scanName) { this.scanName = scanName; }
    public BuySell getBuySell() { return buySell; }
    public void setBuySell(BuySell buySell) { this.buySell = buySell; }
    public Integer getSinceDays() { return sinceDays; }
    public void setSinceDays(Integer sinceDays) { this.sinceDays = sinceDays; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
