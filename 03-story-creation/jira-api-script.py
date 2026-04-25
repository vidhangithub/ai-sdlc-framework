#!/usr/bin/env python3
"""
Jira Story Creation Script — AI-SDLC Framework Phase 03
========================================================
Reads stories from a YAML definition file and creates them in Jira
via the REST API. Supports Epic, Story, and Sub-task creation with
full linking, labels, and acceptance criteria in the description.

Usage:
    python jira-api-script.py --file stories.yaml --dry-run
    python jira-api-script.py --file stories.yaml
    python jira-api-script.py --file stories.yaml --epic PROJ-100

Requirements:
    pip install requests pyyaml python-dotenv
"""

import argparse
import json
import os
import sys
import yaml
import requests
from dotenv import load_dotenv

load_dotenv()

# ─────────────────────────────────────────────
# CONFIGURATION — set via .env or environment variables
# ─────────────────────────────────────────────
JIRA_BASE_URL  = os.getenv("JIRA_BASE_URL", "https://yourcompany.atlassian.net")
JIRA_EMAIL     = os.getenv("JIRA_EMAIL")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
JIRA_PROJECT   = os.getenv("JIRA_PROJECT", "PROJ")


def get_headers():
    import base64
    credentials = base64.b64encode(f"{JIRA_EMAIL}:{JIRA_API_TOKEN}".encode()).decode()
    return {
        "Authorization": f"Basic {credentials}",
        "Content-Type": "application/json",
        "Accept": "application/json"
    }


def validate_config():
    missing = []
    if not JIRA_EMAIL:     missing.append("JIRA_EMAIL")
    if not JIRA_API_TOKEN: missing.append("JIRA_API_TOKEN")
    if missing:
        print(f"[ERROR] Missing environment variables: {', '.join(missing)}")
        print("Create a .env file with these variables or export them.")
        sys.exit(1)


def create_issue(payload: dict, dry_run: bool = False) -> str | None:
    """Create a Jira issue and return its key."""
    if dry_run:
        print(f"  [DRY RUN] Would create: {payload.get('fields', {}).get('summary', 'unknown')}")
        return "PROJ-DRY"

    url = f"{JIRA_BASE_URL}/rest/api/3/issue"
    response = requests.post(url, headers=get_headers(), json=payload, timeout=30)

    if response.status_code == 201:
        key = response.json()["key"]
        print(f"  ✓ Created: {key} — {payload['fields']['summary']}")
        return key
    else:
        print(f"  ✗ Failed [{response.status_code}]: {response.text}")
        return None


def link_issues(inward_key: str, outward_key: str, link_type: str = "is blocked by"):
    """Create an issue link between two Jira issues."""
    url = f"{JIRA_BASE_URL}/rest/api/3/issueLink"
    payload = {
        "type": {"name": link_type},
        "inwardIssue": {"key": inward_key},
        "outwardIssue": {"key": outward_key}
    }
    response = requests.post(url, headers=get_headers(), json=payload, timeout=30)
    if response.status_code == 201:
        print(f"  ✓ Linked: {inward_key} {link_type} {outward_key}")
    else:
        print(f"  ✗ Link failed: {response.text}")


def build_description_adf(story: dict) -> dict:
    """
    Build Atlassian Document Format (ADF) description from story dict.
    Includes: background, acceptance criteria, and DoD.
    """
    content = []

    # Background
    if story.get("background"):
        content.extend([
            {"type": "heading", "attrs": {"level": 3}, "content": [{"type": "text", "text": "Background"}]},
            {"type": "paragraph", "content": [{"type": "text", "text": story["background"]}]}
        ])

    # Acceptance Criteria
    if story.get("acceptance_criteria"):
        content.append({
            "type": "heading", "attrs": {"level": 3},
            "content": [{"type": "text", "text": "Acceptance Criteria"}]
        })
        for i, scenario in enumerate(story["acceptance_criteria"], 1):
            content.append({
                "type": "heading", "attrs": {"level": 4},
                "content": [{"type": "text", "text": f"Scenario {i}: {scenario.get('title', '')}"}]
            })
            content.append({
                "type": "codeBlock", "attrs": {"language": "gherkin"},
                "content": [{"type": "text", "text": scenario.get("gherkin", "")}]
            })

    # NFR Notes
    if story.get("nfr_notes"):
        content.extend([
            {"type": "heading", "attrs": {"level": 3}, "content": [{"type": "text", "text": "NFR Notes"}]},
            {"type": "paragraph", "content": [{"type": "text", "text": story["nfr_notes"]}]}
        ])

    # LRS Reference
    if story.get("lrs_ref"):
        content.append({
            "type": "paragraph",
            "content": [
                {"type": "text", "text": "LRS Reference: ", "marks": [{"type": "strong"}]},
                {"type": "text", "text": story["lrs_ref"]}
            ]
        })

    return {"type": "doc", "version": 1, "content": content}


def create_epic(epic: dict, dry_run: bool) -> str | None:
    """Create a Jira Epic."""
    print(f"\n[EPIC] Creating: {epic['title']}")
    payload = {
        "fields": {
            "project": {"key": JIRA_PROJECT},
            "summary": epic["title"],
            "description": {
                "type": "doc", "version": 1,
                "content": [{"type": "paragraph", "content": [{"type": "text", "text": epic.get("description", "")}]}]
            },
            "issuetype": {"name": "Epic"},
            "labels": epic.get("labels", []),
            "priority": {"name": epic.get("priority", "Medium")},
            # Epic Name field (Jira Next-Gen uses summary; Classic uses customfield_10011)
            "customfield_10011": epic["title"]
        }
    }
    return create_issue(payload, dry_run)


def create_story(story: dict, epic_key: str, dry_run: bool) -> str | None:
    """Create a Jira Story linked to an Epic."""
    print(f"\n  [STORY] {story['title']}")
    payload = {
        "fields": {
            "project": {"key": JIRA_PROJECT},
            "summary": story["title"],
            "description": build_description_adf(story),
            "issuetype": {"name": "Story"},
            "priority": {"name": story.get("priority", "Medium")},
            "story_points": story.get("story_points", 3),
            "labels": story.get("labels", []) + [story.get("lrs_ref", "")],
            # Epic link (classic Jira)
            "customfield_10014": epic_key,
            # Story points (classic Jira)
            "customfield_10016": story.get("story_points", 3)
        }
    }
    return create_issue(payload, dry_run)


def create_subtask(task: dict, parent_key: str, dry_run: bool) -> str | None:
    """Create a sub-task under a Story."""
    print(f"    [TASK] {task['title']}")
    payload = {
        "fields": {
            "project": {"key": JIRA_PROJECT},
            "summary": task["title"],
            "issuetype": {"name": "Sub-task"},
            "parent": {"key": parent_key},
            "priority": {"name": task.get("priority", "Medium")},
            "description": {
                "type": "doc", "version": 1,
                "content": [{"type": "paragraph", "content": [
                    {"type": "text", "text": task.get("description", "")}
                ]}]
            }
        }
    }
    return create_issue(payload, dry_run)


def process_backlog(backlog_file: str, dry_run: bool, epic_override: str | None):
    """Main processing loop — reads YAML and creates all Jira items."""
    with open(backlog_file, "r") as f:
        backlog = yaml.safe_load(f)

    print(f"\n{'[DRY RUN] ' if dry_run else ''}Processing backlog: {backlog_file}")
    print(f"Project: {JIRA_PROJECT} | Base URL: {JIRA_BASE_URL}\n")
    print("=" * 60)

    created_keys = {"epics": [], "stories": [], "tasks": []}

    for epic in backlog.get("epics", []):
        epic_key = epic_override or create_epic(epic, dry_run)
        if not epic_key:
            continue
        created_keys["epics"].append(epic_key)

        for story in epic.get("stories", []):
            story_key = create_story(story, epic_key, dry_run)
            if not story_key:
                continue
            created_keys["stories"].append(story_key)

            for task in story.get("tasks", []):
                task_key = create_subtask(task, story_key, dry_run)
                if task_key:
                    created_keys["tasks"].append(task_key)

    print("\n" + "=" * 60)
    print(f"Summary: {len(created_keys['epics'])} epics, "
          f"{len(created_keys['stories'])} stories, "
          f"{len(created_keys['tasks'])} tasks created.")

    # Output created keys to file for traceability
    if not dry_run:
        with open("created-issues.json", "w") as f:
            json.dump(created_keys, f, indent=2)
        print("Created issue keys saved to: created-issues.json")


def main():
    parser = argparse.ArgumentParser(description="Create Jira stories from YAML definition")
    parser.add_argument("--file", required=True, help="Path to stories YAML file")
    parser.add_argument("--dry-run", action="store_true", help="Preview without creating")
    parser.add_argument("--epic", help="Existing epic key to link stories to (skip epic creation)")
    args = parser.parse_args()

    validate_config()
    process_backlog(args.file, args.dry_run, args.epic)


if __name__ == "__main__":
    main()
