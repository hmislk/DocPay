package org.carecode.docpay.model;

import javax.persistence.*;

@Entity
public class AppSettings {
    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private String hospitalName = "Galle Cooperative Hospital";

    private String addressLine1 = "";
    private String addressLine2 = "";
    private String phone = "";
    private String email = "";

    @Column(nullable = false)
    private String receiptTitle = "Doctor Payment Receipt";

    private String receiptFooter1 = "";
    private String receiptFooter2 = "";

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] logo; // Optional logo image bytes

    // Printer settings
    private String printerName = ""; // empty = system default
    // sizes and margins in millimeters
    private Double pageWidthMm = 210.0;   // A4 default
    private Double pageHeightMm = 297.0;  // A4 default
    private Double marginTopMm = 12.0;
    private Double marginRightMm = 12.0;
    private Double marginBottomMm = 12.0;
    private Double marginLeftMm = 12.0;
    private String orientation = "PORTRAIT"; // or LANDSCAPE
    private Integer scalePercent = 100; // scale content relative to printable area

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getReceiptTitle() { return receiptTitle; }
    public void setReceiptTitle(String receiptTitle) { this.receiptTitle = receiptTitle; }
    public String getReceiptFooter1() { return receiptFooter1; }
    public void setReceiptFooter1(String receiptFooter1) { this.receiptFooter1 = receiptFooter1; }
    public String getReceiptFooter2() { return receiptFooter2; }
    public void setReceiptFooter2(String receiptFooter2) { this.receiptFooter2 = receiptFooter2; }
    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }
    public String getPrinterName() { return printerName; }
    public void setPrinterName(String printerName) { this.printerName = printerName; }
    public Double getPageWidthMm() { return pageWidthMm; }
    public void setPageWidthMm(Double pageWidthMm) { this.pageWidthMm = pageWidthMm; }
    public Double getPageHeightMm() { return pageHeightMm; }
    public void setPageHeightMm(Double pageHeightMm) { this.pageHeightMm = pageHeightMm; }
    public Double getMarginTopMm() { return marginTopMm; }
    public void setMarginTopMm(Double marginTopMm) { this.marginTopMm = marginTopMm; }
    public Double getMarginRightMm() { return marginRightMm; }
    public void setMarginRightMm(Double marginRightMm) { this.marginRightMm = marginRightMm; }
    public Double getMarginBottomMm() { return marginBottomMm; }
    public void setMarginBottomMm(Double marginBottomMm) { this.marginBottomMm = marginBottomMm; }
    public Double getMarginLeftMm() { return marginLeftMm; }
    public void setMarginLeftMm(Double marginLeftMm) { this.marginLeftMm = marginLeftMm; }
    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }
    public Integer getScalePercent() { return scalePercent; }
    public void setScalePercent(Integer scalePercent) { this.scalePercent = scalePercent; }
}
