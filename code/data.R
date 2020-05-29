library(tidyverse)

yields <- read_csv(
    url("https://www.federalreserve.gov/data/yield-curve-tables/feds200628.csv"), 
    skip = 9,
    col_types = cols(
      Date = col_date(format = "%Y-%m-%d"),
      .default = col_double()
    )
  ) %>%
  select(date = Date, y = starts_with("SVENY")) %>%
  group_by(month = month(date), year = year(date)) %>% 
  drop_na() %>%
  summarize(
    y1 = last(y1),
    y2 = last(y2),
    y3 = last(y3),
    y4 = last(y4),
    y5 = last(y5),
    y6 = last(y6),
    y7 = last(y7),
    y8 = last(y8),
    y9 = last(y9),
    y10 = last(y10)
  ) %>%
  arrange(year, month)