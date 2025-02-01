# Stock Search and Chart Application

---

This is a Java-based application that uses SQLite, the Alpha Vantage API, and JFreeChart to search for stocks and display one-month daily closing price charts.

## Features

- **SQLite Database:** Stores stock data.
- **Alpha Vantage API:** Retrieves daily time series data.
- **JFreeChart:** Displays charts for the selected stock.
- **Search Functionality:** Provides an autocomplete search for stocks  
  *(Note: The search feature is still under development and may not work perfectly.)*

## Requirements

- Java 8 or higher
- SQLite JDBC driver
- Alpha Vantage API key

## How to Run

1. **Clone the repository.**
2. **Database:** Place the `mydatabase.db` file in the `src/main/resources` directory.
3. **API Key:** Update the API key in the source code (replace `YOUR_API_KEY` with your actual key).
4. **Build and Run:** Open the project in your preferred IDE (e.g., IntelliJ IDEA) and run the application.

## Known Issues

- The autocomplete search functionality is not fully optimized.

---

Feel free to contribute or open issues if you have any suggestions or encounter problems.
