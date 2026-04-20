USE SERVER_DB;
GO
CREATE TABLE income_from_sells (
    id numeric IDENTITY(1,1) PRIMARY KEY,
    date_aquired date NOT NULL,
    date_sold date NOT NULL,
    symbol varchar(10) NOT NULL,
    security_name varchar(255) NOT NULL,
    isin varchar(20) NOT NULL,
    country varchar(50) NOT NULL,
    quantity numeric NOT NULL,
    cost_basis numeric NOT NULL,
    gross_proceeds numeric NOT NULL,
    gross_pnl numeric NOT NULL,
    currency varchar(10) NOT NULL
);


-- rollback
USE SERVER_DB;
GO
DROP TABLE income_from_sells;