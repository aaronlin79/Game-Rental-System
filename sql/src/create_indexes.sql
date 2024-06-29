-- Catalog table
CREATE INDEX catalogPrice ON Catalog(price);
CREATE INDEX catalogGenre ON Catalog(genre);

-- GamesInOrder table
CREATE INDEX gamesInorderRentalOrder ON GamesInOrder(rentalOrderID);
CREATE INDEX gamesInorderGameid ON GamesInOrder(gameID);

-- RentalOrder table
CREATE INDEX rentalOrderLogin ON RentalOrder(login);
CREATE INDEX rentalOrderTimestamp ON RentalOrder(orderTimestamp);

-- Users table
CREATE INDEX UserRole ON Users(role);