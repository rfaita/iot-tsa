while true; do mosquitto_pub -t /sensor \
  -m "{\"id\":\"1\", \"tenantId\":\"1\", \"timestamp\":$(date +"%s000"), \"temperature\":$(sensors | sed -rn 's/.*Core 0:\s+\+([0-9\.]+).*/\1/p'), \"memory\": $(free -m | awk '/Mem:/ { total=($6/$2)*100 } END { printf("%3.1f\n", total) }')}" \
  -u guest -P guest; sleep 5; done;


while true; do mosquitto_pub -t /sensor \
  -m "{\"id\":\"2\", \"tenantId\":\"1\", \"timestamp\":$(date +"%s000"), \"temperature\":$(sensors | sed -rn 's/.*Core 1:\s+\+([0-9\.]+).*/\1/p'), \"memory\": $(free -m | awk '/Mem:/ { total=($6/$2)*100 } END { printf("%3.1f\n", total) }')}" \
  -u guest -P guest; sleep 5; done;


select mean(temperature), max(temperature), min(temperature), first(temperature), last(temperature) from sensorData_1_1 where time < now() and time > now() -30m group by time(3m) fill(0)
