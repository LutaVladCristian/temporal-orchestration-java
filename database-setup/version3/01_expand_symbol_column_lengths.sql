ALTER TABLE app.income_from_sells
    ALTER COLUMN symbol TYPE varchar(32);

ALTER TABLE app.other_income_fees
    ALTER COLUMN symbol TYPE varchar(32);


-- rollback
ALTER TABLE app.other_income_fees
    ALTER COLUMN symbol TYPE varchar(10);

ALTER TABLE app.income_from_sells
    ALTER COLUMN symbol TYPE varchar(10);
