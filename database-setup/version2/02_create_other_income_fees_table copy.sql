USE SERVER_DB;
GO
CREATE TABLE other_income_fees (
    id numeric IDENTITY(1,1) PRIMARY KEY,
    date_aquired date NOT NULL,
    symbol varchar(10) NOT NULL,
    security_name varchar(255) NOT NULL,
    isin varchar(20) NOT NULL,
    country varchar(50) NOT NULL,
    gross_amount numeric NOT NULL,
    withholding_tax varchar(20) NOT NULL,
    net_amount varchar(20) NOT NULL,
    currency varchar(10) NOT NULL
);


-- rollback
USE SERVER_DB;
GO
DROP TABLE other_income_fees;