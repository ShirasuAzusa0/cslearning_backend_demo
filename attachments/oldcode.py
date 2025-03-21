"""
        if type == 'Category':
            for record in result:
                category = dict(record["category"]) if record["category"] else None
                part = dict(record["part"]) if record["part"] else None
                depart = dict(record["depart"]) if record["depart"] else None
                course1 = dict(record["course1"]) if record["course1"] else None
                course2 = dict(record["course2"]) if record["course2"] else None
                node_list.append({"category":category, "part":part, "depart":depart, "course1":course1, "course2":course2})
        elif type == 'Part':
            for record in result:
                part = dict(record["part"]) if record["part"] else None
                depart = dict(record["depart"]) if record["depart"] else None
                course1 = dict(record["course1"]) if record["course1"] else None
                course2 = dict(record["course2"]) if record["course2"] else None
                node_list.append({"part":part, "depart":depart, "course1":course1, "course2":course2})
        elif type == 'de_Part':
            for record in result:
                depart = dict(record["depart"]) if record["depart"] else None
                course = dict(record["course"]) if record["course"] else None
                node_list.append({"depart":depart, "course":course})
        elif type == 'Course':
            for record in result:
                course = dict(record["course"]) if record["course"] else None
                node_list.append({"course":course})
"""