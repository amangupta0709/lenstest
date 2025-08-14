export default function parsedCron(cron) {
  const parts = cron.trim().split(/\s+/);
  if (parts.length !== 6) throw new Error("Invalid Quartz cron expression");

  const [sec, min, hour, dayOfMonth, month, dayOfWeek] = parts;

  const monthsMap = ["", "January", "February", "March", "April", "May", "June", 
                     "July", "August", "September", "October", "November", "December"];
  const daysMap = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

  const pad = (n) => String(n).padStart(2, "0");
  const timeStr = (h, m) => `${pad(h)}:${pad(m)}`;

  let description = "";

  // --- Handle step values (/), ranges (-), and lists (,)
  const parseField = (val, type) => {
    if (val.includes("/")) {
      const [start, step] = val.split("/");
      if (type === "minute") return `every ${step} minutes${start !== "0" ? ` starting at ${start} past the hour` : ""}`;
      if (type === "hour") return `every ${step} hours${start !== "0" ? ` starting at ${start}:00` : ""}`;
    }
    if (val.includes(",")) return val.split(",").join(" and ");
    if (val.includes("-")) return `from ${val.split("-")[0]} to ${val.split("-")[1]}`;
    return val;
  };

  // --- Time logic ---
  let timePart = "";
  if (hour.includes("/") || min.includes("/") || hour.includes(",") || min.includes(",") || hour.includes("-") || min.includes("-")) {
    timePart = `${parseField(min, "minute")}${hour !== "*" ? `, ${parseField(hour, "hour")}` : ""}`;
  } else if (hour === "*" && min !== "*") {
    timePart = `at ${min} minutes past every hour`;
  } else if (hour !== "*" && min !== "*") {
    timePart = `at ${timeStr(hour, min)}`;
  }

  // --- Day logic ---
  let dayPart = "";
  if (dayOfMonth === "*" && dayOfWeek === "?") {
    dayPart = "every day";
  } else if (dayOfMonth === "?" && dayOfWeek !== "*") {
    if (dayOfWeek.includes("#")) {
      // e.g. 2#3 = third Monday
      const [dow, nth] = dayOfWeek.split("#");
      dayPart = `on the ${nth} ${daysMap[parseInt(dow, 10) - 1]} of every month`;
    } else if (dayOfWeek.includes("L")) {
      // e.g. 5L = last Thursday
      const dow = dayOfWeek.replace("L", "");
      dayPart = `on the last ${daysMap[parseInt(dow, 10) - 1]} of every month`;
    } else {
      const days = dayOfWeek.split(",").map((d) => daysMap[parseInt(d, 10) - 1]).join(", ");
      dayPart = `every ${days}`;
    }
  } else if (dayOfMonth !== "?" && dayOfMonth.includes("L")) {
    if (dayOfMonth === "L") {
      dayPart = "on the last day of every month";
    } else {
      dayPart = `on the ${dayOfMonth.replace("L-", "")} to last day of every month`;
    }
  } else if (dayOfMonth !== "?") {
    dayPart = `on day ${dayOfMonth} of every month`;
  }

  // --- Month logic ---
  let monthPart = "";
  if (month !== "*" && month !== "?") {
    if (month.includes(",")) {
      monthPart = `in ${month.split(",").map((m) => monthsMap[parseInt(m, 10)]).join(", ")}`;
    } else {
      monthPart = `in ${monthsMap[parseInt(month, 10)]}`;
    }
  }

  description = `Run ${timePart} ${dayPart} ${monthPart}`.replace(/\s+/g, " ").trim();
  return description.charAt(0).toUpperCase() + description.slice(1);
}