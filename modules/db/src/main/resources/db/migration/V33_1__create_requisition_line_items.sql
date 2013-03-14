DROP TABLE IF EXISTS requisition_line_items;
CREATE TABLE requisition_line_items (
id                                    SERIAL PRIMARY KEY,
rnrId                                 INT NOT NULL REFERENCES requisitions(id),
productCode                           VARCHAR(50) NOT NULL REFERENCES products(code),
product                               VARCHAR(250) ,
productCategory                       VARCHAR (100),
dispensingUnit                        VARCHAR(20) not null,
beginningBalance                      INTEGER,
quantityReceived                      INTEGER,
quantityDispensed                     INTEGER,
stockInHand                           INTEGER,
quantityRequested                     INTEGER,
reasonForRequestedQuantity            TEXT,
calculatedOrderQuantity               INTEGER,
quantityApproved                      INTEGER,
totalLossesAndAdjustments             INTEGER,
newPatientCount                       INTEGER,
stockOutDays                          INTEGER,
normalizedConsumption                 INTEGER,
amc                                   NUMERIC(14, 4),
maxMonthsOfStock                      INTEGER NOT NULL,
maxStockQuantity                      INTEGER,
packsToShip                           INTEGER,
price                                 NUMERIC(15, 4),
remarks                               TEXT,
dosesPerMonth                         INTEGER NOT NULL,
dosesPerDispensingUnit                INTEGER NOT NULL,
packSize                              SMALLINT NOT NULL,
roundToZero                           BOOLEAN,
packRoundingThreshold                 INTEGER,
fullSupply                            BOOLEAN NOT NULL,
previousStockInHandAvailable          BOOLEAN NOT NULL DEFAULT FALSE,
modifiedBy                            INTEGER,
modifiedDate                          TIMESTAMP  DEFAULT  CURRENT_TIMESTAMP,
creationDate                          TIMESTAMP  DEFAULT  CURRENT_TIMESTAMP
);