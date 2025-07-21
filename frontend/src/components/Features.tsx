import * as React from "react";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import Container from "@mui/material/Container";
import Grid from "@mui/material/Grid";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import AutoFixHighRoundedIcon from "@mui/icons-material/AutoFixHighRounded";
import ConstructionRoundedIcon from "@mui/icons-material/ConstructionRounded";
import QueryStatsRoundedIcon from "@mui/icons-material/QueryStatsRounded";

export const FEATURE_ITEMS = [
  {
    icon: <ConstructionRoundedIcon />,
    title: "Programming without the hastle",
    description:
      "ConGen builds your workout, selecting exercises targeting the specific muscles you give it.",
  },
  {
    icon: <AutoFixHighRoundedIcon />,
    title: "Automatically cycle your exercises",
    description:
      "ConGen alters your exercises automatically, ensuring you progress and preventing staleness.",
  },
  {
    icon: <QueryStatsRoundedIcon />,
    title: "View progression over time",
    description:
      "View your previous workouts and track progress to make sure you are achieving your goals.",
  },
];

export function Features() {
  return (
    <Container
      sx={{
        id: "features",
        position: "relative",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        gap: { xs: 3, sm: 6 },
        pb: { xs: 8, sm: 16 },
      }}
    >
      <Box
        sx={{
          width: { sm: "100%", md: "60%" },
          textAlign: { sm: "left", md: "center" },
        }}
      >
        <Typography component="h2" variant="h4">
          Features
        </Typography>
      </Box>
      <Grid container spacing={2.5}>
        {FEATURE_ITEMS.map((item, index) => (
          <Grid item xs={12} sm={6} md={4} key={index}>
            <Stack
              direction="column"
              color="inherit"
              component={Card}
              spacing={2}
              useFlexGap={true}
              sx={{
                p: 3,
                background: "transparent",
              }}
            >
              <Box sx={{ opacity: "50%" }}>{item.icon}</Box>
              <div>
                <Typography fontWeight="medium" gutterBottom>
                  {item.title}
                </Typography>
                <Typography variant="body2" sx={{ color: "grey.400" }}>
                  {item.description}
                </Typography>
              </div>
            </Stack>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
}
