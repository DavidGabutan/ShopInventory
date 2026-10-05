Answer for question number 1:
For evt_bf3f527d12e929c7 (order TG-DYKP9P), my application checks the eventId before processing an event. The event is stored in the tiangge_events table, which has event_id as a unique value. When the same event came again as sequence 25, the application saw that it was already stored and did not process the order again. If the application restarted between the two deliveries, the stored event would still be in the database, so the second delivery would still be recognized as a duplicate.

Answer for question number 2:
During the restart test, the application was down for about 21,327 seconds while 15 orders arrived. After restarting, the application used the saved cursor in the tiangge_feed_state table to continue reading the Tiangge feed. It was able to find the orders that arrived while the application was offline. It also used the stored event IDs in tiangge_events to avoid processing events that had already been handled.

Answer for question number 3:
My application recognizes a duplicate by checking the event's eventId. The processed event information is stored in the tiangge_events table, where event_id is unique. This information is stored in the database, so it is not lost when the Spring Boot application restarts. This allows the application to safely ignore the same Tiangge event if it is delivered again.